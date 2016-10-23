export class HttpConfig {

  public static configure($httpProvider: any): void {

    // Needed so that Spring Security does not send a WWW-Authenticate header,
    // which will prevent the browser from showing a basic auth popup
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

    // Needed to make sure that the JSESSIONCOOKIE is sent with every request
    $httpProvider.defaults.withCredentials = true;
  }
}
