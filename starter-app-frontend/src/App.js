import React, {useState, useEffect} from 'react';
import './App.css';
import axios from 'axios';
import Button from '@material-ui/core/Button'
import Container from '@material-ui/core/Container'
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import { makeStyles } from '@material-ui/styles';
import { createSlice, configureStore } from 'redux-starter-kit';
import { useSelector } from 'react-redux';

const styles = makeStyles(theme => ({
    mainContent: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '25rem'
    }
}))

const caseSlice = createSlice({
    name: 'case',
    initialState: {text: "placeholder", upper: false},
    reducers: {
        changeCase: state => updateCase(!state.upper),
        caseChange: (state, action) => {
            state.text = action.payload
            state.upper = !state.upper
        }
    }
})

const store = configureStore({
    reducer: caseSlice.reducer
})

const {
    actions: {changeCase, caseChange}
} = caseSlice

const updateCase = (upper) => {
    const url = upper ? 'http://localhost:8080/upper' : 'http://localhost:8080/'
    axios.get(url)
        .then(res => {
            const text = res.data;
            store.dispatch(caseChange(text))
        })
}

function App() {
    const useStyles = styles()

    useEffect(() => {
        changeCase()
    }, [])

    const clickHandler = () => {
        store.dispatch(changeCase())
    }

    return (
        <Container className={useStyles.mainContent} maxWidth="sm">
            <Card>
                <CardContent>{useSelector(state => state.text)}</CardContent>
                <CardActions>
                    <Button onClick={clickHandler} size="small">Smoll</Button>
                </CardActions>
            </Card>
        </Container>
    )
}

export { store }
export default App
