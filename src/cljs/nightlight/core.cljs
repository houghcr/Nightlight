(ns nightlight.core
  (:require [nightlight.editors :as e]
            [nightlight.state :as s]
            [nightlight.repl :as repl]
            [nightlight.components :refer [app]]
            [nightlight.control-panel :as cp]
            [nightlight.ajax :as a]
            [reagent.core :as r]))

(defn check-browser []
  (when (not= -1 (.indexOf js/navigator.userAgent "Edge"))
    (swap! s/runtime-state
      (fn [state]
        (-> state
            (assoc :dialog :unsupported-browser)
            (assoc-in [:options :read-only?] true))))))

(defn init-tree [{:keys [primary-text nested-items selection options]}]
  (cond
    (and (:hosted? options) (not (:read-only? options)))
    (cp/init-status-receiver)
    (not (:hosted? options))
    (a/check-version))
  (swap! s/runtime-state assoc
    :options options
    :title primary-text
    :nodes nested-items
    :reset-count 0)
  (check-browser)
  (e/select-node selection))

(def app-with-init
  (with-meta app
    {:component-did-mount (fn [this]
                            (repl/init-cljs-client)
                            (a/download-state init-tree))}))

(r/render-component [app-with-init] (.querySelector js/document "#app"))

