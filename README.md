# foodtruckz
Posts nearby foodtrucks to a specified web hook

## how to customize
If you want to use this jar for your own purpose you have to 

- change `longitude` and `latitude` and 
- get yourself an Api key from [craftplaces](https://www.craftplaces-business.com/success/loginRegister.php) and create a Kotlin object called `Keys` which contains `const val token: String = "your token"`
- create an incoming web hook for your favorite chat tool and create a Kotlin object called `Hooks` which contains your incoming web hook, e.g. `const val hook: String = "your incoming webhook"`.

This project was tested for the use with [mattermost](https://developers.mattermost.com/integrate/incoming-webhooks/).

