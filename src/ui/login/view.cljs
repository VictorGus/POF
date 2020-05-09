(ns ui.login.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ui.zframes.redirect]
            [ui.login.model :as model]
            [ui.login.form :as form]
            [ui.styles :as styles]
            [ui.basic-components.form.view :as basic-form]
            [ui.pages :as pages]))

(def login-style
  (styles/style
   [:.info-header {:font-size "22px"
                   :font-weight "900"
                   :color "white"
                   :background-color "#0069d9"}]))

(pages/reg-subs-page
 model/index
 (fn [_ _]
   [:div.container.d-flex.h-100 login-style
    [:div.row.align-self-center.w-100
     [:div.col-6.mx-auto
      [:div.card-body
       [:div.card-header.info-header "Sign in form"]
       [:div.card-body.border
        [:label.text-muted "Login"]
        [basic-form/form-input [form/form-path :login]
         "Enter login"]
        [:br]
        [:label.text-muted "Password"]
        [basic-form/form-password [form/form-path :password]
         "Enter password"]
        [:button.btn.btn-outline-primary.btn-block.mt-3.mb-2
         {:on-click #(rf/dispatch [::model/sign-in])}
         "Sign in"]]]]]]))
