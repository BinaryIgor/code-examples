export { };

declare module 'react' {
  namespace JSX {
    interface IntrinsicElements {
      'markets-header': any;
      'assets-and-currencies': any;
      'markets-projections': any;
      'error-modal': any;
    }
  }
}