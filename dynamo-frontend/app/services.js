'use strict';

angular.module('dynamo')

.factory('BackendService', ['$http', '$location', '$websocket', function($http, $location, $websocket){

    var backendHostAndPort = undefined;

    if ($location.host() == 'localhost') {
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
            return 'http://' + backendHostAndPort + imageURL;
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
        }
    }

}])

.factory('UIService', ['$uibModal', 'fileListService', function( $uibModal, fileListService ) {

    var selection;

    return {

        openFileList : function( downloadableId ) {

            var modalInstance = $uibModal.open({
                animation: false,
                templateUrl: 'fileList.html',
                controller: 'FileListCtrl',
                size: 'lg',
                resolve: {
                    fileList: function () {
                        return fileListService.get( downloadableId );
                    }
                }
            });

            modalInstance.result.then(function (selectedItem) {
                selection = selectedItem;
            });
        }, 

        getSelection : function() {
            return selection;
        }

    }

}]);