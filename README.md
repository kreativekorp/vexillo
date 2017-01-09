# Kreative Vexillo

Kreative Vexillo is a repository full of images of flags in various formats, sizes, and aspect ratios, as well as the XML files used to describe them and the computer programs used to generate them.

Flags are named according to ISO 3166-1 alpha-2 country codes. Flags are present for all officially assigned codes as well as many exceptionally reserved, transitionally reserved, and user-assigned codes. Many other flags are included, among them ICS and NATO signal flags, pride flags, and flags for constructed languages.

Kreative Vexillo contains **387 flags** and counting. For comparison, the [My'file flag icon set](http://www.kreativekorp.com/lib/flags/) contains **300 flags** and the original [FamFamFam flag icon set](http://www.famfamfam.com/lab/icons/flags/) contains **247 flags**.

The flag images and the XML files used to describe them are placed in the public domain, free to use for any purpose with no requirement for attribution, except where otherwise noted within the XML file. The source code and binaries used to produce them, as well as any other computer programs in this repository including digital font files, are released under the Mozilla Public License.

----

## [`artwork/vexillo`](https://github.com/kreativekorp/vexillo/tree/master/artwork/vexillo)

The repository of flag images in both SVG and PNG formats in a variety of sizes and aspect ratios. Each size is in its own subdirectory, named according to the following scheme:

  * The first two letters indicate the format:
    * `sm` indicates SVG files.
    * `pm` indicates PNG files.
    * `sg` indicates SVG files with a glossy finish applied, like on FamFamFam flag icons.
    * `pg` indicates PNG files with a glossy finish applied, like on FamFamFam flag icons.
  * The third letter indicates the aspect ratio:
    * `a` indicates an aspect ratio that matches the official proper dimensions of each flag.
    * `c` indicates a 3:2 aspect ratio.
    * `d` indicates a 4:3 aspect ratio, which matches standard non-widescreen monitors.
    * `f` indicates a 1:1 or square aspect ratio.
    * `k` indicates a 16:11 aspect ratio, which matches FamFamFam flag icons.
  * The last three digits indicate the size of the image:
    * `072` indicates a **height** of 72px (or 66px for the `k` series).
    * `128` indicates a **width** of 128px.
    * `360` indicates a **height** of 360px (or 396px for the `k` series).

----

## [`downloads`](https://github.com/kreativekorp/vexillo/tree/master/downloads)

Each series of flag icons is available here as a ZIP archive. The names of the ZIP archives follow the scheme described above. Also available are the `.kvxx` files in a ZIP archive and the Vexillo command line tool JAR.

----

## [`main/java/Vexillo`](https://github.com/kreativekorp/vexillo/tree/master/main/java/Vexillo)

The Java project for the Vexillo command line tools. Run `make` to compile the JAR and `make install` to install the tools in `/usr/local/bin/`. The command line tools include:

  * `vexillo view` or `vexview` - View `.kvxx` files in a window.
  * `vexillo export` or `vexport` - Convert `.kvxx` files to SVG or PNG in any size.
  * `vexillo calc` or `vexcalc` - Calculator.
  * `base64encode` and `base64decode` - Convert files to and from Base64 (used in `.kvxx` and SVG files).
  * `ascii85encode` and `ascii85decode` - Convert files to and from ASCII85 (used in `.kvxx` and PostScript files).

Any of these commands can be run with `--help` for information on their parameters.

----

## [`flags`](https://github.com/kreativekorp/vexillo/tree/master/flags)

The `.kvxx` files containing descriptions of flags. Run the `makeflags` script to generate SVG and PNG files from the `.kvxx` files. The resulting output directory will contain the generated images in the directory structure described above.

----

## [`fonts/Fiavex`](https://github.com/kreativekorp/vexillo/tree/master/fonts/Fiavex)

A TrueType font consisting of vexillological usage and property symbols. The included PDF lists the symbols and PUA code points at which they are encoded.

----

## [`artwork/myfile`](https://github.com/kreativekorp/vexillo/tree/master/artwork/myfile)

The [My'file flag icon set](http://www.kreativekorp.com/lib/flags/) of 300 flags in 16px-by-11px PNG format.

----

## [`artwork/famfamfam`](https://github.com/kreativekorp/vexillo/tree/master/artwork/famfamfam)

The original [FamFamFam flag icon set](http://www.famfamfam.com/lab/icons/flags/) of 247 flags in 16px-by-11px GIF and PNG formats.
