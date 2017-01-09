# Kreative Vexillo
Describe, view, and generate images of flags. Nothing but flags.

This repository currently contains:

[`main/java/Vexillo`](https://github.com/kreativekorp/vexillo/tree/master/main/java/Vexillo)

The Java project for the Vexillo command line tools. Run `make` to compile the JAR and `make install` to install the tools in `/usr/local/bin/`. The command line tools include:

  * `vexillo view` or `vexview` - View `.kvxx` files in a window.
  * `vexillo export` or `vexport` - Convert `.kvxx` files to SVG or PNG in any size.
  * `vexillo calc` or `vexcalc` - Calculator.
  * `base64encode` and `base64decode` - Convert files to and from Base64 (used in `.kvxx` and SVG files).
  * `ascii85encode` and `ascii85decode` - Convert files to and from ASCII85 (used in `.kvxx` and PostScript files).

Any of these commands can be run with `--help` for information on their parameters.

[`flags`](https://github.com/kreativekorp/vexillo/tree/master/flags)

The `.kvxx` files containing descriptions of flags. Run the `makeflags` script to generate SVG and PNG files from the `.kvxx` files. The resulting output directory will contain several subdirectories with the generated images in several sizes and aspect ratios:

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

[`fonts/Fiavex`](https://github.com/kreativekorp/vexillo/tree/master/fonts/Fiavex)

A TrueType font consisting of vexillological usage and property symbols. The included PDF lists the symbols and PUA code points at which they are encoded.
