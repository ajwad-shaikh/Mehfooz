import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { auth, firestore } from '../../firebase';
import { withStyles } from '@material-ui/core/styles';
import authentication from '../../services/authentication';
import { geolocated } from 'react-geolocated';
import GoogleMap from '../GoogleMap';
import { Typography, Grid, Tabs, Tab, Box } from '@material-ui/core';
import PendingTable from '../PendingTable/PendingTable';
import AssignedTable from '../AssignedTable/AssignedTable';
import CompletedTable from '../CompletedTable';

const styles = theme => ({
  buttonIcon: {
    marginRight: theme.spacing(1),
  },
  formGrid: {
    height: '55vh',
    marginLeft: '0px',
    marginRight: '0px',
  },
  header: {
    marginTop: '20px',
    justifyContent: 'center',
    alignItems: 'center',
  },
  mapGrid: {
    marginTop: '75px',
  },
  formControl: {
    margin: theme.spacing(0),
  },
  tabHeader: {
    width: '80%',
    marginLeft: 'auto',
    marginRight: 'auto',
    justifyContent: 'center',
    alignItems: 'center',
  },
});

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <Typography
      component="div"
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && <Box p={3}>{children}</Box>}
    </Typography>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.any.isRequired,
  value: PropTypes.any.isRequired,
};

class HomeContent extends Component {
  constructor(props) {
    super(props);

    this.state = {
      performingAction: false,

      tabValue: 0,
      markerList: [],
    };
  }

  handleTabValueChange = (e, nV) => {
    this.setState({
      tabValue: nV,
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

  populateMarkers() {
    var list = [];
    firestore
      .collection('pending')
      .get()
      .then(snapshot => {
        snapshot.forEach(doc => {
          let marker = doc.data();
          marker.id = doc.id;
          marker.type = 'pending';
          list.push(marker);
        });
        firestore
          .collection('assigned')
          .get()
          .then(snapshot => {
            snapshot.forEach(doc => {
              let marker = doc.data();
              marker.id = doc.id;
              marker.type = 'assigned';
              list.push(marker);
            });
            firestore
              .collection('completed')
              .get()
              .then(snapshot => {
                snapshot.forEach(doc => {
                  let marker = doc.data();
                  marker.id = doc.id;
                  marker.type = 'completed';
                  list.push(marker);
                });
                this.setState({
                  markerList: list,
                });
                console.log(list);
              });
          });
      });
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
    const { /*user,*/ openSnackbar } = this.props;

    const { markerList, tabValue } = this.state;

    function a11yProps(index) {
      return {
        id: `full-width-tab-${index}`,
        'aria-controls': `full-width-tabpanel-${index}`,
      };
    }

    return (
      <Grid container direction="column">
        <Grid item className={classes.header}>
          <Typography variant="h4" align="center">
            Distress Map | The MehfoozNetwork
          </Typography>
        </Grid>
        <Grid item className={classes.formGrid}>
          <GoogleMap mapClicked={this.mapClicked} markerList={markerList} />
        </Grid>
        <Grid item className={classes.tabHeader}>
          <Tabs
            centered
            value={tabValue}
            onChange={this.handleTabValueChange}
            indicatorColor="primary"
            textColor="primary"
            variant="fullWidth"
            aria-label="full width tabs example"
          >
            <Tab label="Pending Distress Response" {...a11yProps(0)} />
            <Tab label="Assigned Distress Response" {...a11yProps(1)} />
            <Tab label="Completed Distress Response" {...a11yProps(2)} />
          </Tabs>
        </Grid>
        <Grid item>
          <TabPanel value={tabValue} index={0}>
            <PendingTable openSnackbar={openSnackbar} />
          </TabPanel>
          <TabPanel value={tabValue} index={1}>
            <AssignedTable openSnackbar={openSnackbar} />
          </TabPanel>
          <TabPanel value={tabValue} index={2}>
            <CompletedTable openSnackbar={openSnackbar} />
          </TabPanel>
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
    this.populateMarkers();
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
