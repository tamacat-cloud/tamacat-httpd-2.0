package cloud.tamacat.httpd.test;

import cloud.tamacat.httpd.Httpd;
import cloud.tamacat.httpd.config.ReverseConfig;
import cloud.tamacat.httpd.config.ServerConfig;
import cloud.tamacat.httpd.config.ServiceConfig;

public class Httpd_test {

	public static void main(String[] args) {
		Httpd.startup(ServerConfig.createAsync().port(80)
			.service(ServiceConfig.create().path("/")
				.reverse(ReverseConfig.create().url("http://localhost:10081/"))));
	}

}
