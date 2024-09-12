(ns undertow-websockets.core
  (:require
   [compojure.core :refer [defroutes GET]]
   [undertow-websockets.lib.ring.undertow :as undertow-adapter]
   [ring.middleware.cors :refer [wrap-cors]]))

(defn handle-ws [_req]
  {:undertow/websocket
   {:on-open (fn [_]
               (println "[ws] open!"))
    :on-message (fn [{:keys [_channel _data]}]
                  (println "[ws] message!"))
    :on-error (fn [{_throwable :error}]
                (println "[ws] error!")
                (println _throwable))
    :on-close (fn [_]
                (println "[ws] close!"))}})

(defroutes routes
  (GET "/ws" [] handle-ws))

(def handler (-> routes
                 (wrap-cors :access-control-allow-origin [#".*"]
                            :access-control-allow-methods [:get :put :post :delete])))

(defn start []
  #_{:clj-kondo/ignore [:inline-def]}
  (def server (undertow-adapter/run-undertow
               handler
               {:host "0.0.0.0"
                :port 8888})))

(defn stop []
  (.stop server))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn restart []
  (stop)
  (start))

(defn -main [& _args]
  (start))
