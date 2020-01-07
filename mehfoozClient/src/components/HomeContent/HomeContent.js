import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { auth, firestore } from '../../firebase';
import { withStyles } from '@material-ui/core/styles';
import authentication from '../../services/authentication';
import { geolocated } from 'react-geolocated';
import GoogleMap from '../GoogleMap';
import {
  Typography,
  Grid,
  TextField,
  Button,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
} from '@material-ui/core';

const styles = theme => ({
  buttonIcon: {
    marginRight: theme.spacing(1),
  },
  formGrid: {
    marginTop: '20px',
    marginLeft: 'auto',
    marginRight: 'auto',
    justifyContent: 'center',
    alignItems: 'center',
  },
  mapGrid: {
    marginTop: '75px',
  },
  formControl: {
    margin: theme.spacing(0),
  },
});

class HomeContent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      performingAction: false,
      center: {
        lat: 23.0168,
        lng: 80.9558,
      },
      zoom: 6,
      markerPosition: {
        lat: 23.0168,
        lng: 80.9558,
      },
      form: {
        name: '',
        contactNumber: '',
        type: '',
      },
    };
  }

  handleNameChange = event => {
    const name = event.target.value;
    var form = this.state.form;
    form.name = name;
    this.setState({
      form: form,
    });
  };

  handleContactNumberChange = event => {
    const contact = event.target.value;
    var form = this.state.form;
    form.contactNumber = contact;
    this.setState({
      form: form,
    });
  };

  handleTypeChange = event => {
    const type = event.target.value;
    var form = this.state.form;
    form.type = type;
    this.setState({
      form: form,
    });
  };

  handleLatChange = event => {
    const latitude = event.target.value;
    var newPosition = this.state.markerPosition;
    newPosition.lat = parseFloat(latitude);
    this.setState({
      markerPosition: newPosition,
    });
  };

  handleLngChange = event => {
    const longitude = event.target.value;
    var newPosition = this.state.markerPosition;
    newPosition.lng = parseFloat(longitude);
    this.setState({
      markerPosition: newPosition,
    });
  };

  sendDistress = () => {
    this.setState({
      performingAction: true,
    });
    firestore
      .collection('pending')
      .add({
        user: this.state.form,
        location: this.state.markerPosition,
        timestamp: new Date(),
      })
      .then(res => {
        this.props.openSnackbar(
          "Distress Lodged Successfully! Hang in there, we're on our way!",
          3,
        );
        this.setState({
          performingAction: false,
        });
      });
  };

  signInWithEmailLink = () => {
    const { user } = this.props;

    if (user) {
      return;
    }

    const emailLink = window.location.href;

    if (!emailLink) {
      return;
    }

    if (auth.isSignInWithEmailLink(emailLink)) {
      let emailAddress = localStorage.getItem('emailAddress');

      if (!emailAddress) {
        this.props.history.push('/');

        return;
      }

      authentication
        .signInWithEmailLink(emailAddress, emailLink)
        .then(value => {
          const user = value.user;
          const displayName = user.displayName;
          const emailAddress = user.email;

          this.props.openSnackbar(
            `Signed in as ${displayName || emailAddress}`,
          );
        })
        .catch(reason => {
          const code = reason.code;
          const message = reason.message;

          switch (code) {
            case 'auth/expired-action-code':
            case 'auth/invalid-email':
            case 'auth/user-disabled':
              this.props.openSnackbar(message);
              break;

            default:
              this.props.openSnackbar(message);
              return;
          }
        })
        .finally(() => {
          this.props.history.push('/');
        });
    }
  };

  setCenter() {
    if (
      this.props.isGeolocationAvailable &&
      this.props.isGeolocationEnabled &&
      this.props.coords
    ) {
      this.setState({
        center: {
          lat: this.props.coords.latitude,
          lng: this.props.coords.longitude,
        },
      });
      console.log(this.state);
    }
  }

  mapClicked = (mapProps, map, clickEvent) => {
    this.setState({
      markerPosition: {
        lat: clickEvent.latLng.lat(),
        lng: clickEvent.latLng.lng(),
      },
    });
  };

  render() {
    // Styling
    const { classes } = this.props;

    // Properties
    // const { user } = this.props;

    const { performingAction, markerPosition, form } = this.state;

    return (
      <Grid container direction="row">
        <Grid item md={6} xs={6} lg={6}>
          <Grid
            container
            spacing={2}
            direction="column"
            className={classes.formGrid}
          >
            <Grid item>
              <Typography variant="h3" color="secondary">
                Stay Mehfooz :)
              </Typography>
            </Grid>
            <Grid item>
              <Typography variant="h5" align="center">
                Complete the form below to register your distress on the
                MehfoozNetwork.
              </Typography>
            </Grid>
            <Grid item>
              <TextField
                autoComplete="name"
                disabled={performingAction}
                label="Name"
                placeholder="John Doe"
                required
                type="text"
                value={form.name}
                variant="outlined"
                onChange={this.handleNameChange}
              />
            </Grid>
            <Grid item>
              <TextField
                autoComplete="contactNumber"
                disabled={performingAction}
                label="Contact Number"
                placeholder="9876543210"
                required
                type="number"
                value={form.contactNumber}
                variant="outlined"
                onChange={this.handleContactNumberChange}
              />
            </Grid>
            <Grid item>
              <Grid container direction="row" spacing={3}>
                <Grid item>
                  <TextField
                    autoComplete="Latitude"
                    disabled={performingAction}
                    label="Latitude"
                    placeholder="23.1025"
                    required
                    type="number"
                    value={markerPosition.lat}
                    variant="outlined"
                    onChange={this.handleLatChange}
                  />
                </Grid>
                <Grid item>
                  <TextField
                    autoComplete="Longitude"
                    disabled={performingAction}
                    label="Longitude"
                    placeholder="80.01225"
                    required
                    type="number"
                    value={markerPosition.lng}
                    variant="outlined"
                    onChange={this.handleLngChange}
                  />
                </Grid>
              </Grid>
            </Grid>
            <Grid item>
              <FormControl component="fieldset" className={classes.formControl}>
                <FormLabel component="legend">Distress Type</FormLabel>
                <RadioGroup
                  row
                  aria-label="type"
                  name="type"
                  value={form.type}
                  disabled={performingAction}
                  onChange={this.handleTypeChange}
                >
                  <FormControlLabel
                    value="health"
                    control={<Radio />}
                    label="Health"
                  />
                  <FormControlLabel
                    value="law_order"
                    control={<Radio />}
                    label="Law & Order"
                  />
                  <FormControlLabel
                    value="disaster"
                    control={<Radio />}
                    label="Disaster"
                  />
                </RadioGroup>
              </FormControl>
            </Grid>
            <Grid item>
              <Button
                color="primary"
                variant="contained"
                disabled={performingAction}
                onClick={this.sendDistress}
              >
                Get Help
              </Button>
            </Grid>
          </Grid>
        </Grid>
        <Grid item md={6} xs={6} lg={6} className={classes.mapGrid}>
          <GoogleMap
            mapClicked={this.mapClicked}
            markerPosition={markerPosition}
          />
          <Typography>
            Click on the map to set an accurate response location
          </Typography>
        </Grid>
      </Grid>
    );
  }

  componentDidMount() {
    this.signInWithEmailLink();
    var user = this.props.user;
    if (user) {
      var form = this.state.form;
      form.name = user.firstName + ' ' + user.lastName;
      form.uid = user.uid;
      this.setState({
        form: form,
      });
    }
  }
}

HomeContent.propTypes = {
  // Styling
  classes: PropTypes.object.isRequired,

  // Properties
  user: PropTypes.object,
};

export default withRouter(
  withStyles(styles)(
    geolocated({
      positionOptions: {
        enableHighAccuracy: true,
        maximumAge: 0,
        timeout: Infinity,
      },
      watchPosition: false,
      userDecisionTimeout: null,
      suppressLocationOnMount: false,
      geolocationProvider: navigator.geolocation,
      isOptimisticGeolocationEnabled: true,
    })(HomeContent),
  ),
);
