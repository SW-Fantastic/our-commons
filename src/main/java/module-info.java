module swdc.commons {

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.apache.commons.compress;
    requires org.tukaani.xz;
    requires org.slf4j;
    requires org.dom4j;
    requires org.apache.httpcomponents.httpclient;
    requires jackrabbit.webdav;
    requires org.apache.httpcomponents.httpcore;

    exports org.swdc.ours.common;
    exports org.swdc.ours.common.annotations;
    exports org.swdc.ours.common.type;
    exports org.swdc.ours.common.network;

}