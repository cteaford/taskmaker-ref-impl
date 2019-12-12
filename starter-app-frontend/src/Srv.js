import axios from 'axios'

export function getShit() {
    return await axios.get('https://jsonplaceholder.typicode.com/users');
}
