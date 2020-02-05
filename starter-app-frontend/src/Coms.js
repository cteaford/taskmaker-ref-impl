import axios from 'axios';

export default class ComServer {
    constructor(fn) {//function to be fired on recieve
        new WebSocket('ws://localhost:8080/').addEventListener('message', (event) => { fn(event.data) })
        this.url = 'http://localhost:8080/api'
    }

    create(payload, byName, then, oopsie) {
        console.log(payload)
        axios.post(`${this.url}/${byName}`, payload)
            .then((resp) => then(resp.data))
            .catch((err) => oopsie(err))


    }

    read(byName, byId, then, oopsie) {
        axios.get(`${this.url}/${byName}/${byId}`)
            .then((resp) => then(resp.data))
            .catch(oopsie)
    }
}
