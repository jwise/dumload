#!/bin/bash

name=dumload
type=svg
#type=png

convert -size 36x36 $name.$type res/drawable-ldpi/$name.png
convert -size 48x48 $name.$type res/drawable-mdpi/$name.png
convert -size 72x72 $name.$type res/drawable-hdpi/$name.png
