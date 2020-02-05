import axios from 'axios';
import { createSlice, configureStore } from 'redux-starter-kit';
import ComServer from './Coms'
import { combineReducers } from 'redux'

const cs = new ComServer((msg) => {
    console.log(msg)
    if(msg === "created") store.dispatch(thingCreated())
})

const thingSlice = createSlice({
    name: 'things',
    initialState: {things: []},
    reducers: {
        addThing: (state, action) => {
            cs.create(action.payload, "thing", (resp) => {console.log(resp)}, (err) => console.log("error", err))
        },
        updateThings: (state, action) => {
            state.things = action.payload
        },
        thingCreated: (state) => {
            cs.read("things", "", (resp) => {store.dispatch(updateThings(resp))}, (err) => console.log("error", err))
        },
    }
})

const rootReducer = combineReducers({
    things: thingSlice.reducer,
})

const store = configureStore({
    reducer: rootReducer,
})

const {
    actions: {addThing, thingCreated, updateThings}
} = thingSlice

export {
    addThing,
    thingCreated,
    store,
}

