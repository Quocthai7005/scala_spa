$(document).ready(function() {
    var serviceGrp = new ServiceGrp();
    ko.applyBindings(serviceGrp);
    serviceGrp.loadServiceGrp();
    serviceGrp.initMessenger();
})

function ServiceGrp() {
    var self = this;
    self.serviceGrps = ko.observable();
    self.loadServiceGrp = function () {
        jsRoutes.controllers.ServiceGrpController.serviceGrp().ajax({
            success: function (data) {
                self.serviceGrps(data)
            },
            error: function (e) {
                console.log(e)
            }
        })
    }

    self.initMessenger = function () {
        jsRoutes.controllers.Application.messenger().ajax({
            success : function(data) {
                data = data.reduce(function(map, obj) {
                    map[obj.name] = obj.content;
                    return map;
                }, {});
                $('.fb-customerchat').attr('page_id', data.pageId);
                $('.fb-customerchat').attr('minimized', data.minimized);
                window.fbAsyncInit = function() {
                    FB.init({
                        appId            : data.appId,
                        autoLogAppEvents : data.autoLogAppEvents == 'true' ? true: false,
                        xfbml            : data.xfbml == 'true' ? true: false,
                        version          : data.version
                    });
                };
                (function(d, s, id){
                    var js, fjs = d.getElementsByTagName(s)[0];
                    if (d.getElementById(id)) {return;}
                    js = d.createElement(s); js.id = id;
                    js.src = "https://connect.facebook.net/en_US/sdk.js";
                    fjs.parentNode.insertBefore(js, fjs);
                }(document, 'script', 'facebook-jssdk'));

            }, error: function(e) {
                console.log(e);
            }
        })
    }
}