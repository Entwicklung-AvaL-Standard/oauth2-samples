const axiosReq = require('../modules/axiosRequestWrapper.js');
const cfg = require('../config/defaults.json');


test('Dummy request ohne OAuth zum Funktionstest', async () => {
    await expect(axiosReq({method: 'get', url: `${cfg["avalstandard-url"]}`, headers: {}})).resolves.toBeDefined();
});