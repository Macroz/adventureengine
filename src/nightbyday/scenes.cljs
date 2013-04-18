(ns nightbyday.scenes
  (:require [crate.core :as crate])
  (:use-macros [crate.def-macros :only [defpartial]]))

(defpartial intro-content-p []
  [:div.content
   [:h1 "Introduction"]
   [:p "It was to be your vacation. Far away from the stressful investigative work, as a detective at the Royal Police Headquarters."]
   [:p "You arrived to Maple-by-river yesterday evening. It was recommended to you by the Chief Inspector as the most peaceful place on Earth. What a shock would it become. But peace would come one way or another."]
   [:p "Staying at the Smiling Sloth Inn, you woke up in the morning to the sounds of alarmed noises outside. Deciding to skip breakfast you headed out through the main hall into the village square."]
   [:p#day1.action "Start your vacation"]])

(defpartial day1-end-content-p []
  [:div.content
   [:h1 "Night"]
   [:p "The kill did not quench your lust. It merely satisfied you for the moment. It delayed the inevitable."]
   [:p "It was time to kill again. And you wanted to start with the nosy police offier. Alex must die tonight."]
   [:p "With grim determination you leave your hideout and enter the village square."]
   [:p#night1.action "Go on with the hunt"]])

(defpartial outro-content-p []
  [:div.content
   [:h1 "Epilogue"]
   [:p "Thank you for playing the murder mystery game " [:strong "Night by Day"]]
   [:p "Made by " [:a {:href "http://markku.rontu.net"} "Markku Rontu"] " / markku.rontu@iki.fi / @zorcam"]
   [:p "Let me know what you think!"]
   [:p "The story continues soon..."]])

(defn intro []
  {:id :intro
   :background {:color "#fff"}
   :objects [{:id :content
              :html [:div.content
                     [:h1 "Introduction"]
                     [:p "It was to be your vacation. Far away from the stressful investigative work, as a detective at the Royal Police Headquarters."]
                     [:p "You arrived to Maple-by-river yesterday evening. It was recommended to you by the Chief Inspector as the most peaceful place on Earth. What a shock would it become. But peace would come one way or another."]
                     [:p "Staying at the Smiling Sloth Inn, you woke up in the morning to the sounds of alarmed noises outside. Deciding to skip breakfast you headed out through the main hall into the village square."]
                     [:p#day1.action "Start your vacation"]]}]})

(defn night1 []
  {:id :night1
   :background {:image "img/night.png" :size [1920 1080]}
   :tasks [{:id :murderalex :name "Murder Alex Thimblewood" :known? true
            :tasks [{:id :preparemurder :name "Prepare for murder" :known? true
                     :tasks [{:id :stealknife :name "Find a knife" :known? true :reveal [:lurealexout]}
                             {:id :lurealexout :name "Lure Alex outside"}]
                     :reveal [:domurder]}
                    {:id :domurder :name "Do it now!"
                     :tasks [{:id :punchthroat :name "Crush his windpipe"}
                             {:id :gougeeyes :name "Gouge his eyes out"}
                             {:id :cutstomach :name "Cut his stomach open"}
                             ]
                     :reveal [:flee :tossknife]}
                    {:id :flee :name "Flee from the site!"
                     :tasks [{:id :tossknife :name "Toss the knife" :reveal [:hide]}
                             {:id :hide :name "Hide"}]}
                    ]}]
   :objects [
             {:id :smilingslothinn
              :position [743 269]
              :size [233 202]
              :name "Smiling Sloth Inn"
              :description "Smiling Sloth Inn is the best, and in fact the only, place for visitors to stay in Maple-on-river. Some patrons are still awake at this time of the night."
              :stealknife {:name "Steal a knife" :description "You sneak in and steal a knife from the kitchen. Just like last night." :result [:knife]}}
             {:id :stables
              :position [1106 386]
              :size [305 110]
              :name "Stables"
              :description "These are the stables of the Smiling Sloth Inn. There are no horses here at the moment. The stableboy must be busy with something else, because he is not here either."
              :hide {:name "Hide in the stables" :description "You sneak into the stables to hide." :result [:hidden]}}
             {:id :policestation
              :position [0 400]
              :size [420 600]
              :name "Police Station"
              :description "The police station guards the square in the middle of Maple-on-river. There is a light in the window. Alex Thimblewood must be there doing the paperwork for the murder case."
              :tossrock {:name "Toss a rock at the window" :description "You toss a small rock at the window hoping to draw Alex out." :result [:alexout]}}
             {:id :generalstore
              :position [1430 330]
              :size [500 340]
              :name "General Store"
              :description "The Good Ol' General Store is the main supplier of goods in Maple-on-river. Most people have their own livestock and gardens for growing vegetables, but the goods produced elsewhere come here. There is a delivery of new goods every week. It is closed for the night."
              :tossknife {:name "Toss the knife" :description "You throw the knife under the porch of the store." :result [:tossedknife]}}
             {:id :alexthimblewood
              :position [675 740]
              :image "img/man2.png"
              :size [168 448]
              :scale 0.3
              :opacity "0"
              :name "Police Officer"
              :description "A burly looking man in a police officer's uniform looking around the square."
              :examine "Alex Thimblewood is the residing police officer of Maple-on-river. He is trying to find the cause of the disturbance."
              :talk "What are you doing here at this time?"
              :punchthroat {:name "Punch his throat" :description "You punch Alex hard in the throat breaking his windpipe and causing him to stumble back!" :result [:crushedwindpipe] :disable [:examine :talk]}
              :cutstomach {:name "Cut his stomach open" :description "You take the knife and slice his stomach open just below the navel. Alex tries to cry out but can't make much noise with his throat in that shape!" :result [:cutthroat]}
              :gougeeyes {:name "Gouge his eyes out" :description "You grab his head with your left hand and force the fingers of your right into his eye socket. You yank out first the right eye and the left and toss them to the ground. Alex falls down in agony." :result [:alexdown]}}
             ]
   :next :end})

(defn day1 []
  {:id :day1
   :background {:image "img/day.png" :size [1920 1080]}
   :tasks [{:id :findoutwhathappened :name "Find out what happened" :known? true
            :tasks [{:id :talk-alexthimblewood :name "Talk to the police" :known? true}]
            :reveal [:helppolice :investigate :talk-to-witness :examine-tallhouse]}
           {:id :helppolice :name "Help police to investigate the murder" :known? false
            :tasks [{:id :investigate :name "Investigate the crime scene" :known? false
                     :tasks [{:id :examine-body :name "Examine the body"}
                             {:id :examine-guts :name "Examine the guts"}
                             {:id :examine-eyes :name "Examine the eyes"}
                             {:id :examine-knife :name "Examine the knife"}
                             {:id :examine-footprints :name "Examine the footprints"}]}
                    {:id :talk-to-witness :name "Talk to witnesses" :known? false
                     :tasks [{:id :talk-johngoodfellow :name "Talk to John"}
                             {:id :talk-eyrikoxhead :name "Talk to Eyrik"}
                             {:id :talk-peterpaulson :name "Talk to Peter"}]}
                    {:id :examine-tallhouse :name "Examine the victim's home" :known? false}]}]
   :objects [
             ;; houses
             {:id :smilingslothinn
              :position [743 269]
              :size [233 202]
              :name "Smiling Sloth Inn"
              :description "Smiling Sloth Inn is the best, and in fact the only, place for visitors to stay in Maple-on-river."}

             {:id :stables
              :position [1106 386]
              :size [305 110]
              :name "Stables"
              :description "These are the stables of the Smiling Sloth Inn. There are no horses here today. The stableboy must be busy with something else, because he is not here either."}
             {:id :tallhouse
              :position [173 159]
              :size [434 470]
              :name "Tall house"
              :description "This is the tallest house in Maple-on-river. Three households live there. There is a shed next to it."
              :examine "David Winterfall owns this house. Or owned it. He also used to live here in the first floor. Something must have drawn his attention for him to come out at night."}
             {:id :shed
              :position [614 420]
              :size [90 130]
              :name "Shed"
              :description "A small shed stands next to the tall house."}
             {:id :policestation
              :position [0 400]
              :size [420 600]
              :name "Police Station"
              :description "The police station guards the square in the middle of Maple-on-river. Regularly one policeman stays here unless there is an emergency."}
             {:id :generalstore
              :position [1430 330]
              :size [500 340]
              :name "General Store"
              :description "The Good Ol' General Store is the main supplier of goods in Maple-on-river. Most people have their own livestock and gardens for growing vegetables, but the goods produced elsewhere come here. There is a delivery of new goods every week. You arrived with the delivery truck yesterday."}
             {:id :johnshouse
              :position [1400 630]
              :size [550 450]
              :name "John's House"
              :description "John the Farmer lives here. He farms most of the land the village owns. His wife Mary is a veterinarian who also herds cattle, sheep and horses with their underlings."}

             ;; crime scene
             {:id :body
              :position [650 600]
              :image "img/body1.png"
              :size [431 255]
              :scale 0.2
              :name "Body"
              :description "The body of a murder victim lies on the edge of the square."
              :examine "The body of a middle-aged man, looks like the mutilated corpse of David Winterfall. The eyes are missing, having been dug out from their sockets. There is severe bruising in the throat area. There is also a gaping whole in the stomach, from where the guts have spilled out. He is obviously dead."}
             {:id :eyes
              :position [700 580]
              :image "img/eyes1.png"
              :size [102 57]
              :scale 0.2
              :name "Eyes"
              :description "A pair of presumably human eyes lies in a pool of blood."
              :examine "The eyes probably belong to the victim. They have been dug out and tossed aside."}
             {:id :guts
              :position [690 620]
              :image "img/blood1.png"
              :size [102 57]
              :scale 0.3
              :name "Guts"
              :description "The guts of the victim have spilled out from his stomach."
              :examine "The gaping wound looks like a cut with a blade."}
             {:id :knife
              :position [420 680]
              :image "img/knife1.png"
              :size [89 48]
              :scale 0.3
              :name "Knife"
              :description "A bloody knife lies on the ground."
              :examine "It looks like a kitchen knife. Possibly the murder weapon."}

             {:id :footprints
              :position [500 640]
              :image "img/tracks1.png"
              :size [327 167]
              :scale 0.25
              :name "Footprints"
              :description "Looks like footprints on the ground."
              :examine "There is some dried up blood in the ground. Perhaps the murdered fled this way."}

             ;; people
             {:id :johngoodfellow
              :position [1000 620]
              :image "img/man1.png"
              :size [236 438]
              :flip true
              :scale [0.22 0.27]
              :name "Farmer"
              :description "John Goodfellow is a tall man with a booming voice. He is farmer by profession and lives just next to the village square."
              :talk "I came out in the morning to go to the fields and saw David lying on the ground. I'm horrified this can happen in our little village."}
             {:id :alexthimblewood
              :position [755 540]
              :image "img/man2.png"
              :size [168 448]
              :flip true
              :scale 0.2
              :name "Police Officer"
              :description "A burly looking man standing in a police officer's uniform."
              :examine "Alex Thimblewood is the residing police officer of Maple-on-river. He is examining the crime scene, looking very concerned."
              :talk "I'm afraid I have sad news. David, the landlord of this tall building, was murdered last night. I heard you are a special detective back at the capital. Might you offer assitance in this matter? Never in my long career have I seen such a horrible crime, let alone in this peaceful village. Please do what you can to help!"}
             {:id :eyrikoxhead
              :position [1130 460]
              :image "img/man2.png"
              :size [168 448]
              :flip true
              :scale 0.15
              :name "Innkeeper"
              :description "Eyrik Oxhead is the owner of the Smiling Sloth Inn. He has come out to see what the commotion is about."
              :talk "I'm glad you are here. With your help, we will catch the guilty quickly!"}
             {:id :peterpaulson
              :position [500 880]
              :image "img/man3.png"
              :size [266 448]
              :scale 0.3
              :name "Councillor"
              :description "Peter Paulson is an esteemed villager and head of the city council."
              :talk "It's such shame. He was a good man and my personal friend, David that is. I have no idea who did it."}
             ]
   :next :night1})
