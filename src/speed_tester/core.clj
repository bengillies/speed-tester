(ns speed-tester.core
	(:require [clojure.string :as s]
		[clj-time.core :as time]
		[clj-time.format :as f])
	(:import [org.webbitserver WebServer WebServers WebSocketHandler]
		[org.webbitserver.handler StaticFileHandler]))

;; Read a random file in and cache it ready for sending out
(def rndData (slurp "rand-100k"))

;; send 10MB worth of data down the websocket
(defn send-10MB [c]
	(dotimes [n 100] ; 100K into 10MB goes 100 times
		(.send c rndData)))

;; receive some data
(defn receive-100K [c]
	(.send c (f/unparse (f/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ") (time/now))))

(defn -main []
	(doto (WebServers/createWebServer 8080)
		(.add "/download"
			(proxy [WebSocketHandler] []
				(onOpen [c] (println "opened download" c))
				(onClose [c] (println "closed download" c))
				(onMessage [c j] (send-10MB c))))
		(.add "/upload"
			(proxy [WebSocketHandler] [] ; ignore data received
				(onOpen [c] (println "opened upload" c))
				(onClose [c] (println "closed upload" c))
				(onMessage [c j] (receive-100K c))))
		(.add (StaticFileHandler. "."))
		(.start)))
