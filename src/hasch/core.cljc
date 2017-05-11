(ns hasch.core
  "Hashing functions for EDN."
  #?(:cljs (:refer-clojure :exclude [uuid]))
  (:require [hasch.benc :refer [PHashCoercion -coerce digest]]
            [hasch.platform :as platform]))

(def uuid4 platform/uuid4)
(def uuid5 platform/uuid5)
(def hash->str platform/hash->str)

(defn edn-hash
  "Hash an edn value with SHA-512 by default or a compatible hash function of choice.

  Please use the write-handlers only in legacy cases and rather extend the PHashCoercion
  protocol to your own types."
  ([val] (edn-hash val {}))
  ([val write-handlers] (edn-hash val hasch.platform/sha512-message-digest write-handlers))
  ([val md-create-fn write-handlers]
   (map #(if (neg? %) (+ % 256) %) ;; make unsigned
        (digest (-coerce val md-create-fn (or write-handlers {})) md-create-fn))))

(defn uuid
  "Creates random UUID-4 without argument or UUID-5 for the argument value.

  Optionally an incognito-style write-handlers map can be supplied,
  which describes record serialization in terms of Clojure data
  structures."
  ([] (uuid4))
  ([val & {:keys [write-handlers]}] (-> val (edn-hash write-handlers) uuid5)))


(defn squuid
  "Calculates a sequential UUID as described in
  https://github.com/clojure-cookbook/clojure-cookbook/blob/master/01_primitive-data/1-24_uuids.asciidoc"
  ([] (squuid (java.util.UUID/randomUUID)))
  ([uuid]
   (let [time (System/currentTimeMillis)
         secs (quot time 1000)
         lsb (.getLeastSignificantBits uuid)
         msb (.getMostSignificantBits uuid)
         timed-msb (bit-or (bit-shift-left secs 32)
                           (bit-and 0x00000000ffffffff msb))]
     (java.util.UUID. timed-msb lsb))))




