module swdc.commons {


    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.compress;
    requires org.slf4j;

    exports org.swdc.ours.common;
    exports org.swdc.ours.common.annotations;
    exports org.swdc.ours.common.type;

}