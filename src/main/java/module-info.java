module swdc.commons {


    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.compress;
    requires org.tukaani.xz;
    requires org.slf4j;
    requires org.dom4j;

    exports org.swdc.ours.common;
    exports org.swdc.ours.common.annotations;
    exports org.swdc.ours.common.type;

}