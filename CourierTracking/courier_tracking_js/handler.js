var request = require("request");

const CLIENT_ID = "YOUR_CLIENT_ID";
const CLIENT_SECRET = "YOUR_CLIENT_SECRET";

var options = {
  method: "POST",
  url: "https://oauth-login.cloud.huawei.com/oauth2/v3/token",
  headers: {
    "Content-Type": "application/x-www-form-urlencoded",
  },
  form: {
    grant_type: "client_credentials",
    client_id: CLIENT_ID,
    client_secret: CLIENT_SECRET,
  },
};

const getPushOptions = (accessToken, deviceToken) => {
  return {
    method: "POST",
    url: "https://push-api.cloud.huawei.com/v1/" + CLIENT_ID + "/messages:send",
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer " + accessToken,
    },
    body: JSON.stringify({
      validate_only: false,
      message: {
        notification: {
          title: "Order Status",
          body: "Your order has been successfully delivered.",
        },
        android: {
          notification: {
            click_action: {
              type: 3,
            },
          },
        },
        token: [deviceToken],
      },
    }),
  };
};

const obtainAccessToken = (callback) => {
  try {
    request(options, function (error, response) {
      if (error) {
        console.log(response.body);
        callback(error);
      } else {
        try {
          callback(JSON.parse(response.body).access_token);
        } catch (error) {
          callback(error);
        }
      }
    });
  } catch (error) {
    callback(error);
  }
};

const pushToken = (accessToken, deviceToken, callback) => {
  const optionsForPush = getPushOptions(accessToken, deviceToken);

  request(optionsForPush, function (error, response) {
    if (error) throw new Error(error);
    console.log(response.body);
    callback(response.body);
  });
};

let myHandler = function (event, context) {
  let eventObject = JSON.parse(event.body);
  try {
    obtainAccessToken((token) => {
      pushToken(token, eventObject.deviceToken, (msg) => {
        context.callback({ msg });
      });
    });
  } catch (error) {
    context.callback({ error });
  }
};

module.exports.myHandler = myHandler;
