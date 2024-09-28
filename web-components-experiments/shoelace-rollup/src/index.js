import '@shoelace-style/shoelace/dist/themes/light.css';
// import '@shoelace-style/shoelace/dist/themes/dark.css';
import SlButton from '@shoelace-style/shoelace/dist/components/button/button.js';
import SlIcon from '@shoelace-style/shoelace/dist/components/icon/icon.js';
import SlInput from '@shoelace-style/shoelace/dist/components/input/input.js';
import SlRating from '@shoelace-style/shoelace/dist/components/rating/rating.js';
import SlSelect from '@shoelace-style/shoelace/dist/components/select/select.js';
import SlOption from '@shoelace-style/shoelace/dist/components/option/option.js';
import SlTextarea from '@shoelace-style/shoelace/dist/components/textarea/textarea.js';
import SlDialog from '@shoelace-style/shoelace/dist/components/dialog/dialog.js';
import { setBasePath } from '@shoelace-style/shoelace/dist/utilities/base-path.js';
import { SomeComponent } from './components.js';

// Set the base path to the folder you copied Shoelace's assets to
setBasePath('/dist/shoelace');

// <sl-button>, <sl-icon>, <sl-input>, and <sl-rating> are ready to use!