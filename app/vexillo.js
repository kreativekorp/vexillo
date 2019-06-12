(function($,window,document,V){

var flagWaverURL = 'https://krikienoid.github.io/flagwaver/#?src=';
var artworkURL = 'https://raw.githubusercontent.com/kreativekorp/vexillo/master/artwork/vexillo';

V.flagmap = {};
var colorCompare = function(a,b){
	for (var j = 0; j < a[3].length && j < b[3].length; j++) {
		if (a[3][j] < b[3][j]) return -1;
		if (a[3][j] > b[3][j]) return 1;
	}
	return a[3].length - b[3].length;
};
for (var i = 0; i < V.flaglist.length; i++) {
	var flag = V.flaglist[i];
	if (flag.colors) flag.colors.sort(colorCompare);
	V.flagmap[flag.id] = flag;
}
for (var i = 0; i < V.encoding.length; i++) {
	var node = V.encoding[i];
	for (var j = 0; j < node[0].length; j++) {
		var id = node[0][j];
		if (V.flagmap[id]) {
			V.flagmap[id].encids = node[0];
			V.flagmap[id].encoding = node[1];
		}
	}
}
for (var i = 0; i < V.keywords.length; i++) {
	var node = V.keywords[i];
	for (var j = 0; j < node[0].length; j++) {
		var id = node[0][j];
		if (V.flagmap[id]) {
			V.flagmap[id].keywords = node[1];
		}
	}
}
for (var i = 0; i < V.namelist.length; i++) {
	var node = V.namelist[i];
	for (var j = 0; j < node[0].length; j++) {
		var id = node[0][j];
		if (V.flagmap[id]) {
			V.flagmap[id].puaname = node[1];
		}
	}
}

var keywordBlacklist = [
	'the', 'a', 'an',
	'flag', 'flags',
	'of', 'for',
	'and', 'or'
];
V.splitKeywords = function(s) {
	s = s.toLowerCase();
	s = s.replace(/[_.']/g, '');
	s = s.split(/[^a-z0-9\[\]]/);
	var words = [];
	for (var i = 0; i < s.length; i++) {
		if (s[i] && keywordBlacklist.indexOf(s[i]) < 0) {
			words.push(s[i]);
		}
	}
	return words;
};

var matchesKeyword = function(keywords, kw) {
	for (var i = 0; i < keywords.length; i++) {
		var whole = '[' + keywords[i] + ']';
		if (whole.indexOf(kw) >= 0) {
			return true;
		}
	}
	return false;
};
var matchesKeywords = function(keywords, kw) {
	for (var i = 0; i < kw.length; i++) {
		if (!matchesKeyword(keywords, kw[i])) {
			return false;
		}
	}
	return true;
};
V.findByKeywords = function(kw) {
	if (kw && kw.length) {
		var found = [];
		for (var i = 0; i < V.flaglist.length; i++) {
			var flag = V.flaglist[i];
			if (matchesKeywords(flag.keywords, kw)) {
				found.push(flag);
			}
		}
		return found;
	} else {
		return V.flaglist;
	}
};

var chr = function(cp) {
	if (String.fromCodePoint) {
		return String.fromCodePoint(cp);
	} else if (cp >= 0 && cp < 0x10000) {
		return String.fromCharCode(cp);
	} else if (cp >= 0x10000 && cp < 0x110000) {
		return String.fromCharCode(
			0xD800 | ((cp - 0x10000) >> 10),
			0xDC00 | (cp & 0x3FF)
		);
	}
};
var chrs = function(cps) {
	if (String.fromCodePoint) {
		return String.fromCodePoint.apply(String, cps);
	} else {
		var s = '';
		for (var i = 0; i < cps.length; i++) {
			s += chr(cps[i]);
		}
		return s;
	}
};
var flagtag = function(s) {
	var cps = [0x1F3F4];
	for (var i = 0; i < s.length; i++) {
		var cp = s.charCodeAt(i);
		if (cp >= 0x30 && cp <= 0x39) cps.push(0xE0000 + cp);
		else if (cp >= 0x41 && cp <= 0x5A) cps.push(0xE0020 + cp);
		else if (cp >= 0x61 && cp <= 0x7A) cps.push(0xE0000 + cp);
	}
	cps.push(0xE007F);
	return cps;
};
var isris = function(cp) {
	return (cp >= 0x1F1E6 && cp <= 0x1F1FF);
};
var ispua = function(cp) {
	return (
		(cp >= 0xE000 && cp < 0xF900) ||
		(cp >= 0xF0000 && cp < 0xFFFFE) ||
		(cp >= 0x100000 && cp < 0x10FFFE)
	);
};
V.createEncRows = function(flag) {
	var plain = [], pua = [], zwj = [], ris = [], tags = [];
	for (var i = 0; i < flag.encids.length; i++) {
		var cps = flagtag(flag.encids[i]);
		if (cps.length > 2) {
			tags.push(['Tag Sequence', chrs(cps), cps]);
		}
	}
	for (var i = 0; i < flag.encoding.length; i++) {
		var cps = flag.encoding[i];
		if (cps.length == 2 && isris(cps[0]) && isris(cps[1])) {
			ris.push(['RIS Sequence', chrs(cps), cps]);
		} else if (cps.length > 2 && cps.indexOf(0x200D) > 0) {
			zwj.push(['ZWJ Sequence', chrs(cps), cps]);
		} else if (cps.length == 1 && ispua(cps[0])) {
			pua.push(['Private Use', chrs(cps), cps]);
		} else if (cps.length == 1 && !ispua(cps[0])) {
			plain.push(['Plain Unicode', chrs(cps), cps]);
		}
	}
	return plain.concat(pua, zwj, ris, tags);
};

V.ujoin = function(cps) {
	var s = 'U';
	for (var i = 0; i < cps.length; i++) {
		var h = cps[i].toString(16).toUpperCase();
		while (h.length < 4) h = '0' + h;
		s += '+' + h;
	}
	return s;
};
V.htmljoin = function(cps) {
	var s = '';
	for (var i = 0; i < cps.length; i++) {
		var h = cps[i].toString(16).toUpperCase();
		s += '&#x' + h + ';';
	}
	return s;
};
V.clickCopy = function(element) {
	$(element).addClass('clickcopy').click(function() {
		var tmp = $('<input>');
		$('body').append(tmp);
		tmp.val($(element).text()).select();
		document.execCommand('copy');
		tmp.remove();
		var bb = $('<div>').text('Copied to clipboard.');
		bb.addClass('butterbar').css('opacity', 0);
		$('body').append(bb);
		bb.animate({'opacity': 1}, 400, function() {
			setTimeout(function() {
				bb.animate({'opacity': 0}, 400, function() {
					bb.remove();
				});
			}, 2000);
		});
	});
};

var getLS = function(key, val) {
	key = 'com.kreative.vexillo.webapp.' + key;
	return window.localStorage && window.localStorage[key] || val;
};
var setLS = function(key, val) {
	key = 'com.kreative.vexillo.webapp.' + key;
	if (window.localStorage) window.localStorage[key] = val;
};

$(document).ready(function() {
	var currentStyleID = getLS('styleId', 'pgc072');
	var currentStyleHeight = parseInt(getLS('styleHeight', 36));
	var currentStyleWidth = parseInt(getLS('styleWidth', 54));
	var currentDLStyle = getLS('dlStyle', 'g');
	var currentDLRatio = getLS('dlRatio', 'c');
	
	var updateStyleSelector = function() {
		$('.style-selector').removeClass('selected');
		$('.style-selector').each(function() {
			var sel = $(this);
			var id = sel.attr('data-styleid');
			var h = parseInt(sel.attr('data-height'));
			var w = parseInt(sel.attr('data-width'));
			if (id === currentStyleID) {
				if (h === currentStyleHeight) {
					if (w === currentStyleWidth) {
						sel.addClass('selected');
					}
				}
			}
		});
	};
	updateStyleSelector();
	
	var updateDLLinks = function() {
		$('.dl-style-selector').removeClass('selected');
		$('.dl-style-selector').each(function(){
			var sel = $(this);
			var id = sel.attr('data-style-code');
			if (id === currentDLStyle) sel.addClass('selected');
		});
		var ars = $('.dl-ar-selectors');
		if (currentDLStyle.length > 1) ars.addClass('dim');
		else ars.removeClass('dim');
		$('.dl-ar-selector').removeClass('selected');
		$('.dl-ar-selector').each(function(){
			var sel = $(this);
			var id = sel.attr('data-ar-code');
			if (id === currentDLRatio) sel.addClass('selected');
		});
		$('.dl-link-panel').addClass('hidden');
		var dllpClass = '.dl-link-panel.dllp-' + currentDLStyle;
		if (currentDLStyle.length < 2) dllpClass += currentDLRatio;
		$(dllpClass).removeClass('hidden');
	};
	updateDLLinks();
	
	var setStyle = function(id, height, width, dlstyle, dlratio) {
		setLS('styleId', currentStyleID = id);
		setLS('styleHeight', currentStyleHeight = height);
		setLS('styleWidth', currentStyleWidth = width);
		if (dlstyle) setLS('dlStyle', currentDLStyle = dlstyle);
		if (dlratio) setLS('dlRatio', currentDLRatio = dlratio);
		updateStyleSelector();
		updateDLLinks();
		$('.tile').each(function() {
			var flagid = $(this).attr('data-flagid');
			var img = $(this).find('img');
			img.attr('src', artworkURL + '/' + id + '/' + flagid + '.png');
			img.attr('height', height);
			img.attr('width', width);
		});
	};
	$('.style-selector').each(function() {
		var sel = $(this);
		var id = sel.attr('data-styleid');
		var h = parseInt(sel.attr('data-height'));
		var w = parseInt(sel.attr('data-width'));
		var dls = sel.attr('data-dlstyle');
		var dlr = sel.attr('data-dlratio');
		sel.bind('click', function(e) {
			setStyle(id, h, w, dls, dlr);
			e.preventDefault();
		});
	});
	
	$('.dl-style-selector').each(function(){
		var sel = $(this);
		var id = sel.attr('data-style-code');
		sel.bind('click', function(){
			setLS('dlStyle', currentDLStyle = id);
			updateDLLinks();
		});
	});
	$('.dl-ar-selector').each(function(){
		var sel = $(this);
		var id = sel.attr('data-ar-code');
		sel.bind('click', function(){
			setLS('dlRatio', currentDLRatio = id);
			updateDLLinks();
		});
	});
	
	var setCurrentTab = function(tabClass) {
		$('.tab').removeClass('selected');
		$('.tab').each(function() {
			var tab = $(this);
			var cls = tab.attr('data-tab-class');
			if (cls === tabClass) tab.addClass('selected');
		});
		$('.tab-content').addClass('hidden');
		$('.tab-content.' + tabClass).removeClass('hidden');
	};
	$('.tab').each(function() {
		var tab = $(this);
		var cls = tab.attr('data-tab-class');
		tab.bind('click', function(){setCurrentTab(cls);});
	});
	
	var openDialog = function(flag) {
		$('.smokescreen').removeClass('hidden');
		/* Flagpole Link */
		var flagURL = artworkURL + '/pma360/' + flag.id + '.png';
		$('.flagpole-link').attr('href', flagWaverURL + encodeURIComponent(flagURL));
		/* Viewer */
		var viewer = $('.flag-viewer img');
		viewer.attr('src', flagURL);
		viewer.attr('height', 180);
		viewer.attr('width', Math.round(180 * flag.ar));
		/* Properties */
		setCurrentTab('properties');
		$('.prop-id').text(flag.id || '\u00A0');
		$('.prop-name').text(flag.name || '\u00A0');
		var propElem = $('.prop-props');
		var propStr = (flag.props ? flag.props.join(', ').replace(/-/g, ' ') : '');
		propElem.text(flag.pcps || '\u00A0');
		propElem.attr('title', propStr);
		$('.prop-ar').text(flag.ars || '\u00A0');
		$('.prop-kw').text(flag.keywords && flag.keywords.join(' ') || '\u00A0');
		/* Downloads */
		$('.dl-style-selector img').each(function(){
			var elem = $(this);
			var id = elem.attr('data-styleid');
			var src = artworkURL + '/' + id + '/' + flag.id + '.png';
			elem.attr('src', src);
		});
		$('.downloads a').each(function(){
			var elem = $(this);
			var tmpl = elem.attr('data-href');
			if (tmpl) {
				var href = tmpl.replace('@FLAGID@', flag.id);
				elem.attr('href', href);
			}
		});
		/* Encoding */
		$('.prop-puaname').text(flag.puaname || '\u00A0');
		var encrows = $('.encoding-rows').empty();
		var erdata = V.createEncRows(flag);
		for (var i = 0; i < erdata.length; i++) {
			var encrow = $('<tr>');
			encrow.append($('<th>').text(erdata[i][0] + ':'));
			encrow.append($('<td>').addClass('code-string').text(erdata[i][1]));
			encrow.append($('<td>').addClass('code-sequence').text(V.ujoin(erdata[i][2])));
			encrow.append($('<td>').addClass('code-sequence').text(V.htmljoin(erdata[i][2])));
			encrows.append(encrow);
		}
		encrows.find('td').each(function(){V.clickCopy(this);});
		/* Colors */
		var colors = $('.colors').empty();
		for (var i = 0; i < flag.colors.length; i++) {
			var c = flag.colors[i];
			var ce = $('<table>');
			var cg = $('<colgroup>');
			cg.append($('<col>').addClass('th'));
			cg.append($('<col>').addClass('td'));
			ce.append(cg);
			var cr = $('<tr>');
			var cc = $('<td>');
			cc.addClass('swatch');
			cc.attr('colspan', 2);
			cc.css('background', c[1]);
			cr.append(cc);
			ce.append(cr);
			for (var j = 0; j < c[2].length; j++) {
				var cs = c[2][j];
				var o1 = cs.indexOf('(');
				var o2 = cs.lastIndexOf(')');
				if (o1 > 0 && o2 > o1) {
					var c1 = cs.substring(0, o1);
					var c2 = cs.substring(o1+1, o2);
					cr = $('<tr>');
					cr.append($('<th>').text(c1));
					cr.append($('<td>').text(c2));
					ce.append(cr);
				} else {
					cr = $('<tr>');
					cc = $('<td>');
					cc.attr('colspan', 2);
					cc.text(cs);
					cr.append(cc);
					ce.append(cr);
				}
			}
			colors.append(ce);
		}
		$('.dialog').removeClass('hidden');
	};
	var closeDialog = function() {
		$('.dialog').addClass('hidden');
		$('.smokescreen').addClass('hidden');
	};
	$('.close-button').bind('click', closeDialog);
	V.clickCopy($('.prop-puaname'));
	
	var makeTile = function(flag, index) {
		var tile = $('<div>').addClass('tile');
		tile.attr('data-flagid', flag.id);
		tile.attr('data-index', index);
		tile.attr('title', flag.name);
		var img = $('<img>');
		img.attr('src', artworkURL + '/' + currentStyleID + '/' + flag.id + '.png');
		img.attr('height', currentStyleHeight);
		img.attr('width', currentStyleWidth);
		img.attr('alt', flag.name);
		tile.append(img);
		tile.bind('click', function(){openDialog(flag);});
		return tile;
	};
	var setTiles = function(flags) {
		var tiles = $('.tiles').empty();
		for (var i = 0; i < flags.length; i++) {
			tiles.append(makeTile(flags[i], i));
		}
	};
	
	var searchInput = $('.search-input');
	var searchString = searchInput.val();
	var searchFn = function() {
		var keywords = V.splitKeywords(searchString);
		var tiles = V.findByKeywords(keywords);
		setTiles(tiles);
	};
	searchFn();
	
	var searchTimeout = null;
	var searchUpdate = function() {
		var s = searchInput.val();
		if (searchString === s) return;
		searchString = s;
		if (searchTimeout) window.clearTimeout(searchTimeout);
		searchTimeout = window.setTimeout(searchFn, 200);
	};
	searchInput.bind('change', searchUpdate);
	searchInput.bind('keydown', searchUpdate);
	searchInput.bind('keyup', searchUpdate);
	
	var menu = $('.menu');
	$('body').bind('click', function() {
		menu.addClass('hidden');
	});
	$('.menu-icon').bind('click', function(e) {
		menu.toggleClass('hidden');
		e.stopPropagation();
	});
	menu.bind('click', function(e) {
		e.stopPropagation();
	});
	$('.menu-item').bind('click', function() {
		menu.addClass('hidden');
	});
	
	$('body').bind('keyup', function(e) {
		if (e.which === 16) $('.sh-acc').removeClass('acc-active');
		if (e.which === 18) $('.alt-acc').removeClass('acc-active');
	});
	$('body').bind('keydown', function(e) {
		if (e.which === 16) $('.sh-acc').addClass('acc-active');
		if (e.which === 18) $('.alt-acc').addClass('acc-active');
		if (e.metaKey || e.ctrlKey) return;
		if (e.which === 27) {
			if ($('.smokescreen').hasClass('hidden')) {
				menu.addClass('hidden');
				searchInput.val('');
			} else {
				closeDialog();
			}
			searchInput.focus();
			e.preventDefault();
			e.stopPropagation();
			return;
		}
		if (!$('.dialog').hasClass('hidden')) {
			if (e.altKey) {
				switch (e.which) {
					case 82: $('.flagpole-link')[0].click(); break;
					case 88: closeDialog(); break;
					case 80: setCurrentTab('properties'); break;
					case 68: setCurrentTab('downloads'); break;
					case 69: setCurrentTab('encoding'); break;
					case 67: setCurrentTab('colors'); break;
					default: return;
				}
				e.preventDefault();
				e.stopPropagation();
				return;
			}
			if (!e.altKey && !$('.downloads').hasClass('hidden')) {
				switch (e.which) {
					case 65: setLS('dlRatio', currentDLRatio = 'a'); break;
					case 66: setLS('dlRatio', currentDLRatio = 'b'); break;
					case 67: setLS('dlRatio', currentDLRatio = 'c'); break;
					case 68: setLS('dlRatio', currentDLRatio = 'd'); break;
					case 69: setLS('dlRatio', currentDLRatio = 'e'); break;
					case 70: setLS('dlRatio', currentDLRatio = 'f'); break;
					case 71: setLS('dlStyle', currentDLStyle = 'g'); break;
					case 72: setLS('dlRatio', currentDLRatio = 'h'); break;
					case 75: setLS('dlRatio', currentDLRatio = 'k'); break;
					case 77: setLS('dlStyle', currentDLStyle = 'm'); break;
					case 86: setLS('dlRatio', currentDLRatio = 'v'); break;
					case 87: setLS('dlRatio', currentDLRatio = 'w'); break;
					case 73: setLS('dlStyle', currentDLStyle = 'vb'); break; // i
					case 79: setLS('dlStyle', currentDLStyle = 'vn'); break; // o
					case 80: setLS('dlStyle', currentDLStyle = 'vf'); break; // p
					default: return;
				}
				updateDLLinks();
				e.preventDefault();
				e.stopPropagation();
				return;
			}
		}
	});
	
	searchInput.focus();
});

})(jQuery,window,document,Vexillo);