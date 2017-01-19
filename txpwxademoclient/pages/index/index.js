//index.js
//获取应用实例
var app = getApp()
Page({
  data: {
    userInfo: {}
  },
  //事件处理函数
  bindViewTap: function () {
    wx.navigateTo({
      url: '../logs/logs'
    })
  },
  onLoad: function () {
    console.log('onLoad')
    var that = this
    //调用应用实例的方法获取全局数据
    app.getUserInfo(function (userInfo) {
      //更新数据
      that.setData({
        userInfo: userInfo
      })
    })
  },
  login: function (e) {
    var that = this;
    wx.login({
      success: function (res) {
        console.log('authorization_code：', res.code);
        if (res.code) {
          //发起网络请求
          wx.request({
            method: 'POST',
            url: 'https://test.tianxiapai.com/wxademo/api/v1/account/login_wx',
            data: {
              code: res.code
            },
            success: function(res) {
              console.log('sessionId：', res.data.sessionId)
            }
          })
        } else {
          console.log('获取用户登录态失败！' + res.errMsg)
        }
      }
    });
  }
})
