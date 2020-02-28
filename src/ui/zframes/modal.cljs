(ns ui.zframes.modal
  (:require [re-frame.core :as rf]
            [ui.styles :as styles]))

(rf/reg-event-db
 ::modal
 (fn [db [_ modal]]
   (assoc db ::modal modal)))

(rf/reg-event-db
 ::close
 (fn [db [_ modal]]
   (dissoc db ::modal)))

(rf/reg-sub
 ::modal
 (fn [db _]
   (::modal db)))

(def modal-styles
  (styles/style
   [:.zmodal {:position "fixed" :height "100%" :top 0
              :width "100%" :z-index "99"
              :font-size "13px"}
    [:.bottom {:margin-top "13px"}]
    [:.modal-content {:box-shadow "0 0px 8px #000"
                      :border-radius "0"
                      :border "none"
                      :background-color "rgb(37, 37, 38)"}]
    [:.modal-header {:background-color "#007acc"
                     :color "white"
                     :border "none"
                     :padding "3px 13px"
                     :font-size "13px"
                     :border-radius "0"}]
    [:.modal-body {:color "#cccccc"
                   :padding "13px"}]]
   ))
(defn modal []
  (let [modal* (rf/subscribe [::modal])]
    (fn []
      (when-let [modal @modal*]
        [:div.zmodal modal-styles
         [:div.modal-fade {:style {:transition "opacity .15s linear"}}
          [:div.modal-dialog.modal-dialog-centered
           {:role "document"
            :on-click #(.preventDefault %)}
           [:div.modal-content
            [:div.modal-header.flex
             [:div.grow (:title modal)]
             [:div.ptbl [:i.fa.fa-times {:on-click (when-not (:persistent modal) #(rf/dispatch [::modal nil]))}]]]

            [:div.modal-body (:body modal)]
            ]]]]))))

(defn confirm-delete [dispatch]
  {:title      "Подтвердите действие"
   :body       "Вы уверены что хотите удалить?"
   :accept     {:text "Да"
                :fn   (fn []
                        (rf/dispatch dispatch))}
   :cancel     {:text "Нет"}
   :persistent true})
