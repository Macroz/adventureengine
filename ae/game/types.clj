(ns ae.game.types)

(def type-hierarchy
  {:object true
   :tool :object
   :battleaxe :weapon
   :mace :weapon
   :morningstar :weapon
   :sword :weapon
   :swordblade :part
   :swordguard :part
   :swordhandle :part
   :swordpommel :part
   :axe :weapon
   :weapon :tool
   :axehead :part
   :axehandle :part
   :part :object
   :leftvambrace :vambrace
   :rightvambrace :vambrace
   :vambrace :armor
   :helmet :armor
   :shield :weapon
   :spear :weapon
   :armor :wearable
   :clothing :wearable
   :wearable :object
   :jacket :clothing
   :leftshoe :shoe
   :rigthshoe :shoe
   :shoe :footwear
   :leftboot :footwear
   :rightboot :footwear
   :footwear :clothing
   :stockings :clothing
   :vest :clothing
   :shirt :clothing
   :pants :clothing
   :sandals :shoes
   :underwear :clothing
   :underdress :clothing
   :pantyhose :underwear
   :panties :underwear
   :corset :underwear
   :garters :underwear
   :gloves :clothing
   :necklace :jewellery
   :jewellery :object
   :bra :underwear
   :dress :clothing
   :nightdress :clothing
   :belt :clothing
   :beard :bodily
   :hair :bodily
   :bodily :object
   :body :bodily
   :man :living
   :living :object
   :mount true
   :bodymount :mount
   :leftfoot :mount
   :rightfoot :mount
   :lefthand :mount
   :righthand :mount
   :leftwrist :mount
   :rightwrist :mount
   :chest :mount
   :groin :mount
   :head :mount
   :mouth :mount})

(defn subtype? [ancestor descendant]
  (if (= ancestor descendant)
    true
    (let [parent (type-hierarchy (keyword descendant))]
      (if (nil? parent)
        false
        (subtype? ancestor parent)))))

(defn object-type? [type]
  (subtype? :object type))

