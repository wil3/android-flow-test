
package com.github.wil3.android.flowtests;

import java.net.URL;

public class IP {
        private String ip;
        private String about;
        private URL url;

        public String getIp() {
            return ip;
        }

        public String getAbout() {
            return about;
        }

        public URL getPro() {
            return url;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public void setPro(URL url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "JsonIp [ip=" + ip + ", about=" + about
                    + ", Pro!=" + url + "]";
        }
    }