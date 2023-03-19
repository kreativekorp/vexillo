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

V.regions = [
	['North America',         ['north', 'america'                                   ]],
	['Central America',       ['north', 'central', 'america'                        ]],
	['Caribbean',             ['north', 'america', 'caribbean'                      ]],
	['Native American',       ['native', 'american'                                 ]],
	['South America',         ['south', 'america'                                   ]],
	['Northern Europe',       ['northern', 'europe'                                 ]],
	['Western Europe',        ['western', 'europe'                                  ]],
	['Southern Europe',       ['southern', 'europe'                                 ]],
	['Eastern Europe',        ['eastern', 'europe'                                  ]],
	['Central Asia',          ['central', 'asia'                                    ]],
	['East Asia',             ['east', 'asia'                                       ]],
	['South Asia',            ['south', 'asia'                                      ]],
	['Southeast Asia',        ['southeast', 'asia'                                  ]],
	['Southwest Asia',        ['western', 'southwestern', 'asia'                    ]],
	['North Africa',          ['north', 'africa'                                    ]],
	['East Africa',           ['east', 'africa'                                     ]],
	['Central Africa',        ['central', 'africa'                                  ]],
	['Southern Africa',       ['southern', 'africa'                                 ]],
	['West Africa',           ['west', 'africa'                                     ]],
	['Australasia',           ['oceania', 'australasia'                             ]],
	['Melanesia',             ['oceania', 'melanesia'                               ]],
	['Micronesia',            ['oceania', 'micronesia'                              ]],
	['Polynesia',             ['oceania', 'polynesia'                               ]],
	['Antarctica',            ['antarctica'                                         ]],
	['International',         ['int', 'international'                               ]],
	['Constructed Languages', ['conlang', 'constructed', 'language'                 ]],
	['Pride',                 ['pride'                                              ]],
	['Uncategorized',         [                                                     ]],
	['Organizational',        ['org', 'organization'                                ]],
	['Miscellaneous',         ['misc', 'miscellaneous'                              ]],
	['ICS',                   ['ics', 'international', 'code', 'signals'            ]],
	['NATO',                  ['nato', 'north', 'atlantic', 'treaty', 'organization']],
	['Esperanto',             ['esperanto'                                          ]],
	['Semaphore',             ['semaphore'                                          ]],
	['Racing',                ['sport', 'sports', 'racing'                          ]],
	['Generic',               ['generic', 'general'                                 ]],
	['Kreative Media',        ['kreative', 'entertainment', 'media'                 ]],
];
var copyRegions = function(regions) {
	var ra = [];
	for (var i = 0; i < regions.length; i++) {
		var orig = regions[i];
		var copy = [orig[0], orig[1], []];
		ra.push(copy);
	}
	return ra;
};
var arrayStartsWith = function(arr, subarr) {
	for (var i = 0; i < subarr.length; i++) {
		if (arr[i] !== subarr[i]) {
			return false;
		}
	}
	return true;
};
var regionForKeywords = function(regions, keywords) {
	var rr = null;
	var rrkw = null;
	for (var i = 0; i < regions.length; i++) {
		var cr = regions[i];
		var crkw = cr[1];
		if (!rrkw || crkw.length > rrkw.length) {
			if (arrayStartsWith(keywords, crkw)) {
				rr = cr;
				rrkw = crkw;
			}
		}
	}
	return rr;
};
V.regionsForFlags = function(regions, flags) {
	var ra = copyRegions(regions);
	for (var i = 0; i < flags.length; i++) {
		var r = regionForKeywords(ra, flags[i].keywords);
		if (r) r[2].push(flags[i]);
	}
	return ra;
};

V.blocks = [
	[0xE000, 0xE07F, 'Generic Flags',                   {size: 0}],
	[0xE080, 0xE0FF, 'Signal Flags',                    {size: 0}],
	[0xE100, 0xE3FF, 'ISO Country Code Flags',          {size: 0}],
	[0xE400, 0xE4FF, 'North American Flags',            {size: 0}],
	[0xE500, 0xE5FF, 'British Flags',                   {size: 0}],
	[0xE600, 0xE6FF, 'Scandinavian Flags',              {size: 0}],
	[0xE700, 0xE7FF, 'Iberian Flags',                   {size: 0}],
	[0xE800, 0xE8FF, 'Western European Flags-A',        {size: 0}],
	[0xE900, 0xE9FF, 'Western European Flags-B',        {size: 0}],
	[0xEA00, 0xEAFF, 'Western European Flags-C',        {size: 0}],
	[0xEB00, 0xEBFF, 'Eastern European Flags-A',        {size: 0}],
	[0xEC00, 0xECFF, 'Eastern European Flags-B',        {size: 0}],
	[0xED00, 0xEDFF, 'Eastern European Flags-C',        {size: 0}],
	[0xEE00, 0xEEFF, 'Asian Flags-A',                   {size: 0}],
	[0xEF00, 0xEFFF, 'Asian Flags-B',                   {size: 0}],
	[0xF000, 0xF0FF, 'South American Flags',            {size: 0}],
	[0xF100, 0xF1FF, 'African Flags',                   {size: 0}],
	[0xF200, 0xF2FF, 'Oceanic Flags',                   {size: 0}],
	[0xF300, 0xF3FF, 'Miscellaneous Subdivision Flags', {size: 0}],
	[0xF400, 0xF4FF, 'Constructed Language Flags',      {size: 0}],
	[0xF500, 0xF5FF, 'Pride Flags',                     {size: 0}],
	[0xF600, 0xF6FF, 'Sports Flags',                    {size: 0}],
	[0xF700, 0xF7FF, 'Kreative Media Flags',            {size: 0}],
	[0xF800, 0xF8FF, 'Miscellaneous Flags',             {size: 0}],
];
for (var i = 0; i < V.encoding.length; i++) {
	var node = V.encoding[i];
	var flag = null;
	for (var j = 0; j < node[0].length; j++) {
		var id = node[0][j];
		var f = V.flagmap[id];
		if (f) {
			flag = f;
			break;
		}
	}
	if (flag) {
		for (var j = 0; j < node[1].length; j++) {
			var cps = node[1][j];
			if (cps.length == 1 && ispua(cps[0])) {
				var cp = cps[0];
				for (var k = 0; k < V.blocks.length; k++) {
					var block = V.blocks[k];
					if (cp >= block[0] && cp <= block[1] && !block[3][cp]) {
						block[3][cp] = flag;
						block[3].size++;
					}
				}
			}
		}
	}
}

var getQueryParams = function() {
	var qp = {};
	var qa = window.location.search.substring(1).split('&');
	for (var i = 0; i < qa.length; i++) {
		if (qa[i].length) {
			var qo = qa[i].indexOf('=');
			if (qo > 0) {
				var qk = decodeURIComponent(qa[i].substring(0, qo));
				qp[qk] = decodeURIComponent(qa[i].substring(qo + 1));
			} else {
				qp[decodeURIComponent(qa[i])] = true;
			}
		}
	}
	return qp;
};
var getQueryParamKeys = function() {
	var qk = [];
	var qa = window.location.search.substring(1).split('&');
	for (var i = 0; i < qa.length; i++) {
		if (qa[i].length) {
			var qo = qa[i].indexOf('=');
			if (qo > 0) {
				qk.push(decodeURIComponent(qa[i].substring(0, qo)));
			} else {
				qk.push(decodeURIComponent(qa[i]));
			}
		}
	}
	return qk;
};
var setQueryParam = function(key, val, push) {
	var qp = getQueryParams();
	if (qp[key] === val) return;
	qp[key] = val;
	var qa = [];
	var qk = getQueryParamKeys();
	if (qk.indexOf(key) < 0) qk.push(key);
	for (var i = 0; i < qk.length; i++) {
		if (qp[qk[i]] === undefined || qp[qk[i]] === false) {
			continue;
		} else if (qp[qk[i]] === true) {
			qa.push(encodeURIComponent(qk[i]));
		} else {
			var qv = encodeURIComponent(qp[qk[i]]);
			qa.push(encodeURIComponent(qk[i]) + '=' + qv);
		}
	}
	var qs = qa.length ? ('?' + qa.join('&')) : window.location.href.split('?')[0];
	if (window.location.hash) qs += window.location.hash;
	if (push) {
		if (window.history.pushState) window.history.pushState({}, '', qs);
	} else {
		if (window.history.replaceState) window.history.replaceState({}, '', qs);
	}
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
	var qp = getQueryParams();
	var currentArrange = getLS('arrange', 'id');
	var currentStyleID = getLS('styleId', 'pgc072');
	var currentStyleHeight = parseInt(getLS('styleHeight', 36));
	var currentStyleWidth = parseInt(getLS('styleWidth', 54));
	var currentDLStyle = getLS('dlStyle', 'g');
	var currentDLRatio = getLS('dlRatio', 'c');
	if (qp['a']) {
		var sel = $('.arrange-selector[data-arrange="' + qp['a'] + '"]');
		if (sel.length) currentArrange = qp['a'];
	}
	if (qp['s']) {
		var sel = $('.style-selector[href="?s=' + qp['s'] + '"]');
		if (sel.length) {
			currentStyleID = sel.attr('data-styleid');
			currentStyleHeight = parseInt(sel.attr('data-height'));
			currentStyleWidth = parseInt(sel.attr('data-width'));
			currentDLStyle = sel.attr('data-dlstyle') || currentDLStyle;
			currentDLRatio = sel.attr('data-dlratio') || currentDLRatio;
		}
	}
	
	var updateStyleSelector = function() {
		$('.tiles').attr('class', 'tiles ' + currentStyleID);
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
		var qv = sel.attr('href').split('=')[1];
		var id = sel.attr('data-styleid');
		var h = parseInt(sel.attr('data-height'));
		var w = parseInt(sel.attr('data-width'));
		var dls = sel.attr('data-dlstyle');
		var dlr = sel.attr('data-dlratio');
		sel.bind('click', function(e) {
			setQueryParam('s', qv);
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
		setQueryParam('t', tabClass);
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
		tab.bind('click', function(e) {
			setCurrentTab(cls);
			e.preventDefault();
		});
	});
	
	var openDialog = function(flag) {
		setQueryParam('d', flag.id);
		$('.smokescreen').removeClass('hidden');
		/* Flagpole Link */
		var flagURL = artworkURL + '/pma360/' + flag.id + '.png';
		$('.flagpole-link').attr('href', flagWaverURL + encodeURIComponent(flagURL));
		/* Viewer */
		var viewer = $('.flag-viewer img');
		viewer.attr('src', flagURL);
		viewer.attr('height', 180);
		viewer.attr('width', Math.round(180 * flag.ar));
		/* Tabs */
		$('.tab').each(function() {
			var tab = $(this);
			var cls = tab.attr('data-tab-class');
			tab.attr('href', '?d=' + flag.id + '&t=' + cls);
		});
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
		/* ACQR */
		var acqrURL = artworkURL + '/pvx240/' + flag.id + '.png';
		$('.acqr img').attr('src', acqrURL);
		$('.dialog').removeClass('hidden');
	};
	var closeDialog = function() {
		setQueryParam('t', false);
		setQueryParam('d', false);
		$('.dialog').addClass('hidden');
		$('.smokescreen').addClass('hidden');
	};
	$('.close-button').bind('click', closeDialog);
	V.clickCopy($('.prop-puaname'));
	
	var makeTile = function(flag, index) {
		var tile = $('<a>').addClass('tile');
		tile.attr('data-flagid', flag.id);
		tile.attr('data-index', index);
		tile.attr('title', flag.name);
		tile.attr('href', '?d=' + flag.id);
		var img = $('<img>');
		img.attr('src', artworkURL + '/' + currentStyleID + '/' + flag.id + '.png');
		img.attr('height', currentStyleHeight);
		img.attr('width', currentStyleWidth);
		img.attr('alt', flag.name);
		tile.append(img);
		tile.bind('click', function(e) {
			openDialog(flag);
			e.preventDefault();
		});
		return tile;
	};
	var setTiles = function(flags) {
		var tiles = $('.tiles').empty();
		for (var i = 0; i < flags.length; i++) {
			tiles.append(makeTile(flags[i], i));
		}
	};
	var setRegions = function(regions, flags) {
		var tiles = $('.tiles').empty();
		var index = 0;
		var ra = V.regionsForFlags(regions, flags);
		for (var i = 0; i < ra.length; i++) {
			if (ra[i][2].length) {
				tiles.append($('<h1>').addClass('region-header').text(ra[i][0]));
				for (var j = 0; j < ra[i][2].length; j++) {
					tiles.append(makeTile(ra[i][2][j], index++));
				}
			}
		}
	};
	
	var makeCodePointSet = function(flags) {
		var cpset = {};
		for (var i = 0; i < flags.length; i++) {
			var enc = flags[i].encoding;
			if (enc) {
				for (var j = 0; j < enc.length; j++) {
					var cps = enc[j];
					if (cps.length == 1 && ispua(cps[0])) {
						cpset[cps[0]] = true;
					}
				}
			}
		}
		return cpset;
	};
	var makeBlockHeader = function(block) {
		var s = block[0].toString(16).toUpperCase();
		var e = block[1].toString(16).toUpperCase();
		var r = $('<code>').text(s + '-' + e);
		var n = $('<span>').text(' \u00A0 ' + block[2]);
		var h = $('<h1>');
		h.addClass('block-header');
		h.append(r);
		h.append(n);
		return h;
	};
	var makeHexHeader = function() {
		var row = $('<tr>');
		row.append($('<th>'));
		for (var i = 0; i < 16; i++) {
			var h = i.toString(16).toUpperCase();
			row.append($('<th>').text(h));
		}
		return row;
	};
	var makeHexRow = function(i) {
		var row = $('<tr>');
		var h = (i >> 4).toString(16).toUpperCase();
		row.append($('<th>').text(h));
		return row;
	};
	var setEncTables = function(flags) {
		var matches = makeCodePointSet(flags);
		var cells = $('.tiles .block-cell');
		if (cells.length) {
			cells.each(function(){
				var cell = $(this);
				var cp = parseInt(cell.find('.tile').attr('data-index'));
				if (matches[cp]) cell.removeClass('block-cell-nonmatch');
				else cell.addClass('block-cell-nonmatch');
			});
			return;
		}
		var tiles = $('.tiles').empty();
		for (var i = 0; i < V.blocks.length; i++) {
			var block = V.blocks[i];
			if (block[3].size) {
				tiles.append(makeBlockHeader(block));
				var table = $('<table>');
				table.attr('cellpadding', 0);
				table.attr('cellspacing', 0);
				table.addClass('block-table');
				table.append(makeHexHeader());
				for (var j = block[0]; j <= block[1]; j += 16) {
					var row = makeHexRow(j);
					for (var k = 0; k < 16; k++) {
						var cell = $('<td>');
						if (block[3][j+k]) {
							cell.addClass('block-cell');
							if (!matches[j+k]) cell.addClass('block-cell-nonmatch');
							cell.append(makeTile(block[3][j+k], j+k));
						} else {
							cell.addClass('block-cell-unassigned');
						}
						row.append(cell);
					}
					table.append(row);
				}
				tiles.append(table);
			}
		}
	};
	
	var searchInput = $('.search-input');
	if (qp['q']) searchInput.val(qp['q']);
	var searchString = searchInput.val();
	var searchKeywords = V.splitKeywords(searchString);
	var searchResults = V.findByKeywords(searchKeywords);
	var showResults = function() {
		switch (currentArrange) {
			default: setTiles(searchResults); break;
			case 'geo': setRegions(V.regions, searchResults); break;
			case 'cp': setEncTables(searchResults); break;
		}
	};
	showResults();
	
	var searchTimeout = null;
	var searchUpdate = function() {
		var s = searchInput.val();
		if (searchString === s) return;
		searchString = s;
		searchKeywords = V.splitKeywords(searchString);
		searchResults = V.findByKeywords(searchKeywords);
		if (searchTimeout) window.clearTimeout(searchTimeout);
		searchTimeout = window.setTimeout(function() {
			setQueryParam('q', s || false);
			showResults();
		}, 200);
	};
	searchInput.bind('change', searchUpdate);
	searchInput.bind('keydown', searchUpdate);
	searchInput.bind('keyup', searchUpdate);
	
	var updateArrangeSelector = function() {
		$('.arrange-selector').removeClass('selected');
		$('.arrange-selector').each(function() {
			var sel = $(this);
			var id = sel.attr('data-arrange');
			if (id === currentArrange) sel.addClass('selected');
		});
	};
	updateArrangeSelector();
	
	$('.arrange-selector').each(function() {
		var sel = $(this);
		var id = sel.attr('data-arrange');
		sel.bind('click', function(e) {
			setQueryParam('a', id);
			setLS('arrange', currentArrange = id);
			showResults();
			updateArrangeSelector();
			e.preventDefault();
		});
	});
	
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
					case 65: setCurrentTab('acqr'); break;
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
	
	if (qp['d'] && V.flagmap[qp['d']]) {
		openDialog(V.flagmap[qp['d']]);
		if (qp['t']) {
			var tab = $('.tab[data-tab-class="' + qp['t'] + '"]');
			if (tab.length) setCurrentTab(qp['t']);
		}
	} else {
		searchInput.focus();
	}
});

})(jQuery,window,document,Vexillo);