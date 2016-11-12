'use strict';

angular.module('dynamo')

.factory('BackendService', ['$http', '$location', '$websocket', function($http, $location, $websocket){

    var backendHostAndPort = undefined;

    if ($location.host() == 'localhost') {
        //backendHostAndPort = '192.168.1.75:8081';
        backendHostAndPort = 'localhost:8081';
    } else {
        backendHostAndPort = $location.host() + ':' + location.port;
    }

    return {

        getWebSocket : function( url ) {
            return $websocket('ws://' + backendHostAndPort + '/websocket/' + url);
        },

        getBackendURL : function() {
            return 'http://' + backendHostAndPort + '/services/';
        },

        getImageURL : function( imageURL ) {
            return 'http://' + backendHostAndPort + '/services' + imageURL;
        },

        post: function( url, data ) {
            var completeURL = this.getBackendURL() + url;
            return $http.post( completeURL, data );
        },

        delete: function( urlPrefix, parameters ) {
            var completeURL = this.getBackendURL() + urlPrefix;
            if (parameters) {
                completeURL += ( '?' +  Object.keys(parameters).map(function(key) { return encodeURIComponent(key) + '=' + encodeURIComponent(parameters[key]); }).join('&') );
            }
            return $http.delete( completeURL );
        },   

        get: function( urlPrefix, parameters ) {
            var completeURL = this.getBackendURL() + urlPrefix;
            if (parameters) {
                completeURL += ( '?' +  Object.keys(parameters).map(function(key) { return encodeURIComponent(key) + '=' + encodeURIComponent(parameters[key]); }).join('&') );
            }
            return $http.get( completeURL );
        },

        getAndCache: function( urlPrefix, parameters ) {
            var completeURL = this.getBackendURL() + urlPrefix;
            if (parameters) {
                completeURL += ( '?' +  Object.keys(parameters).map(function(key) { return encodeURIComponent(key) + '=' + encodeURIComponent(parameters[key]); }).join('&') );
            }
            return $http.get( completeURL, {cache: true} );
        },
    }

}]);