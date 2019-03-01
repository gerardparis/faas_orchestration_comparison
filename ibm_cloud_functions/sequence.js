const composer = require('openwhisk-composer')

module.exports = composer.repeat(5, 'fsleep1s')
