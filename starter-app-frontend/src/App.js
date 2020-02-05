import React, {useState, useEffect} from 'react';
import './App.css';
import Button from '@material-ui/core/Button'
import Container from '@material-ui/core/Container'
import Card from '@material-ui/core/Card';
import CardActions from '@material-ui/core/CardActions';
import CardContent from '@material-ui/core/CardContent';
import { makeStyles } from '@material-ui/styles';
import { useSelector } from 'react-redux';
import { store, addThing } from './State'
import { Input, TextField } from '@material-ui/core';

const styles = makeStyles(theme => ({
    mainContent: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-evenly',
        height: '25rem'
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between'
    }
}))

function App() {

    const useStyles = styles()

    const makeThing = (e) => {
        e.preventDefault()
        store.dispatch(addThing({ name: e.target.thang.value }))
    }

    const things = useSelector(s => s.things.things)

    return (
        <Container className={useStyles.mainContent} maxWidth="sm">
            <form onSubmit={makeThing} className={useStyles.form} noValidate autoComplete="off">
                <TextField id="thing-name" name="thang" label="Name your thing"></TextField>
                <Input type="submit" value="Make a thing"></Input>
            </form>
            <Card>
                <CardContent>My Things</CardContent>
                {things.map(t => <CardContent>{t.name}</CardContent>)}
            </Card>
        </Container>
    )
}

export default App
