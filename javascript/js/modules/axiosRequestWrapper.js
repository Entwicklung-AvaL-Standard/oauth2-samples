const axios = require('axios').default;
const https = require('https');
const fs = require('fs');
var rootCas = require('ssl-root-cas').create();
rootCas.push(fs.readFileSync(__dirname + '/../../cert/godaddy-avalstandard.pem').toString("utf-8"));
rootCas.push(fs.readFileSync(__dirname + '/../../cert/godaddy-root2.pem').toString("utf-8"));
const httpsAgent = new https.Agent({
    ca: rootCas
});
const axiosInstance = axios.create({httpsAgent});
/**
 * @param {object} opts request options
 * @param {string} opts.url endpoint URL
 * @param {object} opts.headers  HTTP headers, can be string or object
 * @param {string} opts.xml SOAP envelope, can be read from file or passed as string
 * @param {int} opts.timeout Milliseconds before timing out request
 * @param {object} opts.proxy Object with proxy configuration
 * @param {int} opts.maxContentLength Limit content/body size being sent(bytes)
 * @param {object} opts.extraOpts Object of additional axios parameters
 * @promise response
 * @reject {error}
 * @fulfill {body,statusCode}
 * @returns {Promise.response{body,statusCode}}
 */
module.exports = async function axiosRequest(opts = {
    method: '',
    url: '',
    headers: {},
    content: '',
    timeout: 10000,
    proxy: {},
    maxContentLength: Infinity,
    extraOpts: {},
}) {
    const {
        method,
        url,
        headers,
        content,
        timeout,
        proxy,
        maxContentLength,
        extraOpts,
    } = opts;
    return await new Promise((resolve, reject) => {
        axiosInstance({
            method,
            url,
            headers,
            data: content,
            timeout,
            proxy,
            maxContentLength,
            ...extraOpts,
        }).then((response) => {
            resolve({
                response: {
                    headers: response.headers,
                    body: response.data,
                    statusCode: response.status,
                }
            });
        }).catch((error) => {
            if (error.response) {
                console.error(`FAIL: ${error}`);
                reject(error.response);
            } else {
                console.error(`FAIL: ${error}`);
                reject(error);
            }
        });
    });
};
