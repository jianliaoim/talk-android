! function(root) {
  "use strict"

  var navigator = root.navigator.userAgent,
      actions = ["device.version", "biz.chat", "biz.createTopic", "biz.uploadImage", "biz.scanQrcode", "biz.getLocation"];

  var parseRoutes = function(action, handler) {
    var sections = action.split(".");
    for (var i = 0, len = sections.length, container = talk; len > i; i++) {
      if(i === len - 1) {
        container[sections[i]] = handler
      }
      if( "undefined" == typeof container[sections[i]]) {
        container[sections[i]] = {}
      }
      container = container[sections[i]]
    }
  }

  var attachHandler = function(action, passedIn) {
    if ("undefined" == typeof WebViewJavascriptBridge) {
      return console.log("WebViewJavascriptBridge is undefined");
    }
    var params = {}
    var callback = function(res) {
      res = JSON.parse(res);
      if (true === res.success) {
        console.log('success');
        var foo = passedIn.onSuccess || function(data) { console.log(data) };
        foo(res.data);
      } else {
        var onFail = passedIn.onFail || function(err) { console.error(err.code + ':' + err.msg) };
        onFail(res.error);
      }
    }

    switch(action) {
      case 'device.version':
        break;
      case 'biz.chat':
        if(undefined == passedIn.contactId && undefined == passedIn.topicId) {
          console.error('Target id needed');
          return;
        } else {
          if(passedIn.contactId) {
            params = {
              isPrivate: true,
              targetId: passedIn.contactId
            }
          }
          if(passedIn.roomId) {
            params = {
              targetId: passedIn.roomId
            }
          }
        }
        break;
      case 'biz.createTopic':
        break;
      case 'biz.uploadImage':
        break;
      case 'biz.scanQrcode':
        break;
      case 'biz.getLocation':
        break;
      default:
        console.error('Function is not defined');
        break;
    }
    WebViewJavascriptBridge.callHandler(action, params, callback);
  }


  var connectWebViewJavascriptBridge = function(callback) {
    if (window.WebViewJavascriptBridge) {
      callback(WebViewJavascriptBridge);
    } else {
      document.addEventListener(
        'WebViewJavascriptBridgeReady'
        , function() {
          callback(WebViewJavascriptBridge);
        },
        false
      );
    }
  }

  function initBridge() {
    connectWebViewJavascriptBridge(function(bridge) {
      bridge.init(function(message, responseCallback) {
        console.log('JS got a message', message);
        var data = {
          'Javascript Responds': 'Wee!'
        };
        // responseCallback(data);
      });

      onBridgeReady();
    });
  }

  function onBridgeReady() {
    console.log('WebViewJavascriptBridge is ready');
    initEvents();
  }

  var talk = {
    ios: /iPhone|iPad|iPod/i.test(navigator),
    android: /Android/i.test(navigator),
    version: '0.0.0',
    jsApiList: [],
    config: function(config) {
      if(true) {
        this.jsApiList = config.jsApiList;
      } else {
        this.onFail();
      }
    },
    onReady: function(callback) {
      initBridge();
      callback && callback();
    },
    onFail: function(callback) {
      console.error('Initialization failed');
      callback && callback();
    }
  }

  function initEvents() {
    talk.jsApiList.forEach(function(action) {
      parseRoutes(action, function(passedIn) {
        attachHandler(action, passedIn);
      })
    })
  }

  "object" == typeof module && module && "object" == typeof module.exports ? module.exports = talk : "function" == typeof define && (define.amd || define.cmd) ? define("talk", [], function() { return talk }) : root.talk = talk
}(this);
