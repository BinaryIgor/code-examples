// Import here all shoelace assets and components that you intend to use and have in final index.js bundle
import '@shoelace-style/shoelace/dist/themes/light.css';
import SlButton from '@shoelace-style/shoelace/dist/components/button/button.js';
import SlIcon from '@shoelace-style/shoelace/dist/components/icon/icon.js';
import SlInput from '@shoelace-style/shoelace/dist/components/input/input.js';
import SlSelect from '@shoelace-style/shoelace/dist/components/select/select.js';
import SlOption from '@shoelace-style/shoelace/dist/components/option/option.js';
import SlDialog from '@shoelace-style/shoelace/dist/components/dialog/dialog.js';
import { setBasePath } from '@shoelace-style/shoelace/dist/utilities/base-path.js';

// Set the base path to the folder you copied Shoelace's assets to
setBasePath('/dist/shoelace');
