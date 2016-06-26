'use strict';

angular.module('dynamo')

.factory('BackendService', ['backendHostAndPort', '$http', function(backendHostAndPort, $http){

    return {
        getBackendURL : function() {
            return 'http://' + backendHostAndPort + '/services/';
        },

        get: function( urlPrefix ) {
            return $http.get( this.getBackendURL() + urlPrefix );
        }
    }

}]);