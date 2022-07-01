(defn unqualify
  "Strip the namespace from a keyword."
  [kw]
  (-> kw name keyword))
(type unqualify [:fn [:keyword] :unqualified-keyword])
