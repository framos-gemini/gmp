package edu.gemini.aspen.gmp.tcsoffset.model;

public enum Dir {
    MARK(0),
    CLEAR(1),
    PRESET(2),
    START(3),
    STOP(4);
        private final int _type;
        private Dir(int type) {_type = type;}
        public int getType() {return _type;}
        public static edu.gemini.aspen.gmp.tcsoffset.model.Dir getFromInt(int nType) {
            for (edu.gemini.aspen.gmp.tcsoffset.model.Dir e : edu.gemini.aspen.gmp.tcsoffset.model.Dir.values()) {
                if (e.getType() == nType)
                    return e;
            }
            throw new IllegalArgumentException("No Offset type with the id "+ nType);
        }

}
