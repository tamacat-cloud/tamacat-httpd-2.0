package cloud.tamacat.httpd.test;

import cloud.tamacat.httpd.ClassicHttpd;
import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;

public class ClassicHttpd_test {

	public static void main(String[] args) {
		ClassicHttpd.startup(ServerConfig.create().port(80)
			.service(ServiceConfig.create().path("/")
				.reverse(ReverseConfig.create().url("http://localhost:10081/"))));
	}

}
