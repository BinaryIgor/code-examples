import path from 'path';
import copy from 'rollup-plugin-copy';
import css from 'rollup-plugin-css-only'
import resolve from '@rollup/plugin-node-resolve';

export default {
  input: 'index.js',
  output: [{ dir: path.resolve(__dirname, 'dist'), format: 'es', compact: true }],
  plugins: [
    resolve(),
    // Bundle styles into dist/shoelace.css
    css({
      output: 'shoelace.css' 
    }),
    // Copy Shoelace assets to dist/shoelace; copy HTMX as well
    copy({
      copyOnce: true,
      targets: [
        {
          src: path.resolve(__dirname, 'node_modules/@shoelace-style/shoelace/dist/assets'),
          dest: path.resolve(__dirname, 'dist/shoelace')
        },
        {
          src: path.resolve(__dirname, 'node_modules/htmx.org/dist'),
          dest: path.resolve(__dirname, 'dist/htmx')
        }
      ]
    })
  ]
};