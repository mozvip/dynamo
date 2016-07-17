'use strict';

angular.module('dynamo')

.factory('BackendService', ['backendHostAndPort', '$http', function(backendHostAndPort, $http){

    return {
        getBackendURL : function() {
            return 'http://' + backendHostAndPort + '/services/';
        },

        getImageURL : function( imageURL ) {
            return 'http://' + backendHostAndPort + imageURL;
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