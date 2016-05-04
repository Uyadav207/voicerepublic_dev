(ns vrng.venue
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :include-macros true]
   [ajax.core :refer [PUT]]
   goog.string.format
   goog.string))

;;      [cljs.core.async :as async
;;    :refer [<! >! chan close! sliding-buffer put! alts! timeout]])
;;  (:require-macros [cljs.core.async.macros :as m :refer [go alt! go-loop]]))

;; -------------------------
;; state

(defonce state
  (atom (js->clj (.-initialSnapshot js/window) :keywordize-keys true)))

(defonce translations
  (atom (js->clj (.-translations js/window))))

;; -------------------------
;; generic utils

(defn t [& key]
  (let [keys (flatten (map #(clojure.string/split % ".") key))]
    (reduce get @translations keys)))

(defn millis [datetime]
  (.parse js/Date datetime))

(defn format-countdown [millis]
  (let [base    (/ (Math/abs millis) 1000)
        minute  60
        hour    (* 60 minute)
        day     (* 24 hour)
        days    (int (/ base day))
        hours   (int (/ base hour))
        minutes (- (int (/ base minute)) (* hours minute))
        seconds (- (int base) (* hours hour) (* minutes minute))]
    (cond
      (< millis 0) (str (goog.string.format
                         "%02dh %02dm %02ds"
                         hours minutes seconds) " over due")
      (> hours 48) (str days " " (t "days"))
      :else (goog.string.format
             "%02dh %02dm %02ds"
             hours minutes seconds))))

;; -------------------------
;; Page specific utils

(defn venue-slug []
  (:slug (:venue @state)))

(defn venue-url []
  (str "/venues/" (venue-slug)))

(defn venue-state []
  (:state (:venue @state)))

(defn venue-name []
  (:name (:venue @state)))


(defn talks-by-state [talk-state]
  (filter #(= talk-state (:state %)) (vals (:talks (:venue @state)))))

(defn active-talk []
  (or (first (talks-by-state "live"))
      (last (sort :starts_at (talks-by-state "prelive")))))

(defn active-talk-title []
  (:title (active-talk)))

(defn active-talk-state []
  (:state (active-talk)))


(defn time-to-active-talk []
  (- (millis (:starts_at (active-talk))) (* 1000 (:now @state))))

(defn time-to-available []
  (* (- (:availability (:venue @state)) (:now @state)) 1000))

(defn time-since-provisioning []
  (- (* (:now @state) 1000) (millis (:completed_provisioning_at (:venue @state)))))


(defn icecast-url []
  (:stream_url (:venue @state)))


;;(defn talk-comp [talk]
;;  ^{:key (talk :id)}
;;  [:li
;;   (talk :starts_at)
;;   (talk :title)
;;   "(" (talk :id) ")"])
;;
;;(defn talk-listing [talk-state]
;;  [:ul
;;   (doall
;;    (map talk-comp (talks-by-state talk-state)))])
;;
;;(defn talk-section [talk-state]
;;  [:div
;;   [:h3 (t "talk_state" talk-state)]
;;   [talk-listing talk-state]])

;; -------------------------
;; Messageing

(defn update-atom [atom-data]
  (prn "Snapshot:" atom-data)
  (reset! state atom-data))

(defn venue-message-handler [msg]
  (condp = (:event msg)
    "snapshot" (update-atom (:snapshot msg))
    (prn "Unknown message" msg)))

;; -------------------------
;; Helpers

(defn talk-url [talk]
  (str "/xhr/talks/" (talk :id)))

;; TODO make variadic with &
(defn venue-in-state [venue-states]
  (some #(= (venue-state) %) venue-states))

(defn active-talk-in-state [active-talk-states]
  (some #(= (active-talk-state) %) active-talk-states))

;; -------------------------
;; Actions

(defn request-availability-action []
  (PUT (venue-url)
       {:format :json
        :params { :venue { :event "become_available" }}}))

(defn start-server-action []
  (PUT (venue-url)
       {:format :json
        :params { :venue { :event "start_provisioning" }}}))

(defn select-device-action [event]
  (let [device-id (.. event -target -value)]
    (PUT (venue-url)
         {:format :json
          :params { :venue {:device_id device-id
                            :event "select_device" }}})))

(defn start-talk-action [talk]
  (PUT (talk-url talk)
       {:format :json
        :params { :talk { :event "start_talk!"}}}))

;; -------------------------
;; Business Logic

(defn server-status []
  (cond
    (venue-in-state ["select_device" "awaiting_stream"
                     "connected" "disconnected"]) "success"
    (venue-in-state ["provisioning"]) "warning"
    (< (time-to-active-talk) (* 4 60 1000)) "alert"
    (venue-in-state ["available"]) "warning"
    :else "neutral"))

(defn device-status []
  (cond
    (venue-in-state ["provisioning"]) "warning"
    (venue-in-state ["select_device" "disconnected"]) "alert"
    (venue-in-state ["connected"]) "success"
    :else "neutral"))

(defn talk-status []
  (cond
    (and (active-talk-in-state ["live"])
         (venue-in-state ["connected"])) "success"
    (< (time-to-active-talk) 0) "alert"
    (< (time-to-active-talk) (* 10 60 1000)) "warning"
    :else "neutral"))

;; -------------------------
;; Components

(defn audio-comp []
  [:audio { :auto-play "autoplay" }
   [:source { :src (icecast-url) :data-x 1 }]])

(defn countdown-comp [millis]
  [:span (format-countdown millis)])

(defn server-start-button-comp []
  [:button { :on-click start-server-action } (t "setup")])

;;(defn talk-start-button-comp [talk]
;;  [:button { :on-click #(start-talk-action talk) } (t "start")])

(defn titlebar-comp [title status]
  [:div {:class "titlebar clearfix"}
   [:h4 {:class "title"} title
    [:span {:class (str status " badge float-right")}
     (condp = status
       "success" (goog.string/unescapeEntities "&#x2713;")
       "neutral" "-"
       "warning" "!"
       "alert" "!")]]])

(defn server-actionbar-comp []
  [:div {:class "actionbar clearfix"}
   (cond
     (venue-in-state ["offline"])
     [:div {:class "holder"}
      [:p {:class "launchable-in float-left"} "Available in "
       [:span {:class "countdown", :id "time-till-provisioning"}
        [countdown-comp (time-to-available)]]]
      [:a {:class "button success tiny float-right server-start-button disabled"} "LAUNCH SERVER"]]

     (venue-in-state ["available"])
     [:div {:class "holder"}
      [:p {:class "launch-advice float-left"} "Start Server Now!"]
      [:a {:class "button success tiny float-right server-start-button"
           :on-click start-server-action} "LAUNCH SERVER"]]

     (venue-in-state ["provisioning"])
     [:div {:class "secondary progress server-progress", :role "progressbar",
            :tabindex "0", :aria-valuenow "25", :aria-valuemin "0",
            :aria-valuetext "25 percent", :aria-valuemax "100"}
      [:div {:class "progress-meter", :style { :width "25%" }}
       [:p {:class "progress-meter-text"} "Server is starting"]]]

     :else ;; all other
     [:div {:class "holder clearfix"}
      [:p {:class "label online-for"}] [countdown-comp (time-since-provisioning)]
      [:span {:class "float-right server-bitrate-wrapper hide"}
       [:span {:class "server-bitrate"} "--"] " kB/sec"]])])

(defn device-option-comp [device]
  ^{:key (device :id)}
  [:option {:value (device :id)} (device :name)])

(defn device-list-comp []
  [:select {:on-change #(select-device-action %)}
   (doall (map device-option-comp (:devices @state)))])

(defn device-actionbar-comp []
  [:div {:class "actionbar clearfix"}
   [device-list-comp]])

(defn talk-actionbar-comp []
  [:div {:class "actionbar clearfix"}
   (cond
     (active-talk-in-state ["prelive"])
     [:div {:class "holder talk-phase2"}
      [:span {:class "label scheduled-start"} "Scheduled Start: "
       (format-countdown (time-to-active-talk))
       [:span {:class "clock-msg"}]
       [:span {:class "ago-msg hide"}]]
      ;; add disabled as a link class if setup not ready
      [:a {:class "button success tiny talk-start-button float-right"
           :on-click #(start-talk-action (active-talk))} "Start Broadcast"]]

     (active-talk-in-state ["live"])
     [:div {:class "secondary progress talk-progress", :role "progressbar", :tabindex "0", :aria-valuenow "25", :aria-valuemin "0", :aria-valuetext "25 percent", :aria-valuemax "100"}
      [:div {:class "progress-meter", :style { :width "3%"}}
       [:p {:class "progress-meter-text"}]]
      [:a {:href "#", :class "button tiny alert talk-stop-button hollow has-tip tip-top", :data-tooltip "", :aria-haspopup "true", :data-disable-hover "false", :tabindex "1", :title "Are you sure you want to stop the stream early?"} "Stop Broadcast"]])])

(defn active-talk-title-prefix-comp []
  [:span {:class "talk-label"}
   (if (= (active-talk-state) "prelive")
     "NEXT UP: "
     "CURRENT TALK: ")])

(defn debug-device-comp [device]
  ^{:key (device :id)}
  [:li (:id device)])

(defn debug-comp []
  [:ul { :style { :color "magenta" } }
   [:li "venue state: " (venue-state)]
   [:li "now: " (:now @state)]
   [:li "avail: " (time-to-available) " - " (format-countdown (time-to-available))]
   [:li "active: " (time-to-active-talk) " - " (format-countdown (time-to-active-talk))]
   [:li "provis: " (time-since-provisioning) " - " (format-countdown (time-since-provisioning))]
   [:li "devices" [:ul (doall (map debug-device-comp (:devices @state)))]]
   ])

(defn on-air-comp []
  (let [on (active-talk-in-state ["live"])]
    [:span {:class (str "label on-air float-right" (if on " success" ""))}
     (if on "ON AIR" "OFF AIR")]))

(defn audio-meter-comp []
  [:span {:class "soundbars float-right"}
   [:div {:id "bars"}
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]
    [:div {:class "bar-holder"}
     [:div {:class "bar"}]]]])

(defn root-comp []
  [:div {:id "page"}
   (if (venue-in-state ["connected"]) [audio-comp])
   ;;[debug-comp]
   [:div {:id "venue-header", :class "clearfix row expanded"}
    [:div {:class "medium-6 columns"}
     [:h1 {:class "venue-title"} (venue-name)]
     [:p {:class "talk-title"}
      [active-talk-title-prefix-comp] (active-talk-title)]]
    [:div {:class "medium-6 columns dashboard"}
     [on-air-comp]
     [audio-meter-comp]
     [:div {:class "float-right listeners hide"}
      [:span {:class "listener-count"} "210"]
      [:span {:class "listener-label"} "LISTENERS"]]]]
   [:div {:class "row clearfix"}
    [:div {:id "venue-control-panel", :class "clearfix"}
     [:div {:class "medium-4 columns server-panel"}
      [titlebar-comp (t "streaming_server") (server-status)]
      [:div {:class "divider"}]
      [server-actionbar-comp]]
     [:div {:class "medium-4 columns device-panel"}
      [titlebar-comp (t "audio_source") (device-status)]
      [:div {:class "divider"}]
      [device-actionbar-comp]]
     [:div {:class "medium-4 columns talk-panel"}
      [titlebar-comp (t "broadcast") (talk-status)]
      [:div {:class "divider"}]
      [talk-actionbar-comp]]]]])

;; -------------------------
;; Initialize

(defn inc-now [state-map]
  (update-in state-map [:now] inc))

(defn start-timer []
  (js/setInterval #(swap! state inc-now) 1000))

(defn venue-channel []
  (:channel (:venue @state)))

(defn setup-faye []
  (print "Subscribe" (venue-channel))
  (.subscribe js/fayeClient (venue-channel)
              #(venue-message-handler (js->clj %  :keywordize-keys true))))

(defn mount-root []
  (reagent/render [root-comp] (.getElementById js/document "app")))


(defn init! []
  (start-timer)
  (setup-faye)
  (if (= (venue-state) "offline")
    (js/setTimeout request-availability-action (max 2000 (time-to-available))))
  (mount-root))

;; https://www.bignerdranch.com/blog/music-visualization-with-d3-js/


;; talk listing
