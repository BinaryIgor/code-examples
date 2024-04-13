#!/bin/bash

input_css_path=static/styles.css
output_css_path=static/live-styles.css
export stylesPath=$output_css_path

./tailwindcss -i $input_css_path -o $output_css_path --watch