const composer = require('openwhisk-composer')

module.exports = composer.repeat(5, 'fpasspayload')
