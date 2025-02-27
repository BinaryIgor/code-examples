import { eventBus } from "./event-bus";

export const Events = {
  SHOW_ERROR_MODAL: "SHOW_ERROR_MODAL",
  USER_SIGNED_IN: "USER_SIGNED_IN",
  USER_SIGNED_OUT: "USER_SIGNED_OUT",
  SHOW_CONFIRMABLE_MODAL: "SHOW_CONFIRMABLE_MODAL",
  REFRESH_USER_DATA: "REFRESH_USER_DATA",

  showErrorModal(error, params = {}) {
    eventBus.publish({ type: this.SHOW_ERROR_MODAL, data: { error, errorParams: params } });
  },
  showErrorModalOrRun(response, onSuccess, errorParams={}) {
    if (response.success) {
      onSuccess();
    } else {
      this.showErrorModal(response.error(), errorParams);
    }
  },
  showErroModalIfFailure(response) {
    this.showErrorModalOrRun(response, () => { });
  },
  showConfirmableModal(modalId = "") {
    eventBus.publish({ type: this.SHOW_CONFIRMABLE_MODAL, data: modalId });
  },
  userSignedIn() {
    eventBus.publish({ type: this.USER_SIGNED_IN });
  },
  userSignedOut() {
    eventBus.publish({ type: this.USER_SIGNED_OUT });
  }
};
