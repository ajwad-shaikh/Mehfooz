import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { firestore } from '../../firebase';
import { withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { Button } from '@material-ui/core';

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
  performingAction: false,
});

class AssignedTable extends Component {
  constructor(props) {
    super(props);

    this.state = {
      performingAction: false,

      tabValue: 0,
      assignedList: [],
    };
  }

  fetchAssignedDistress() {
    var list = [];
    firestore
      .collection('assigned')
      .get()
      .then(snapshot => {
        snapshot.forEach(doc => {
          let distress = doc.data();
          distress.id = doc.id;
          list.push(distress);
        });
        this.setState({
          assignedList: list,
        });
      });
  }

  handleMarkCompleted(id) {
    console.log(id);
    this.setState({
      performingAction: true,
    });
    var moveDoc = '';
    const here = this;
    firestore
      .collection('assigned')
      .doc(id)
      .get()
      .then(doc => {
        moveDoc = doc.data();
        firestore
          .collection('completed')
          .doc(id)
          .set(moveDoc)
          .then(() => {
            firestore
              .collection('assigned')
              .doc(id)
              .delete()
              .then(() => {
                here.setState({
                  performingAction: false,
                });
                here.props.openSnackbar(
                  'Distress Marked as Completed Successfully!',
                );
                window.location.reload();
              });
          });
      });
  }

  render() {
    // Styling
    const { classes } = this.props;

    // Properties
    // const { user, markerList } = this.props;

    const { assignedList, performingAction } = this.state;

    return (
      <TableContainer component={Paper}>
        <Table className={classes.table} aria-label="simple table">
          <TableHead>
            <TableRow>
              <TableCell>User Name</TableCell>
              <TableCell align="center">Timestamp</TableCell>
              <TableCell align="center">Location</TableCell>
              <TableCell align="center">Contact Number</TableCell>
              <TableCell align="center">Distress Type</TableCell>
              <TableCell align="center">Action</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {assignedList.map(row => {
              return (
                <TableRow key={row.id}>
                  <TableCell component="th" scope="row">
                    {row.user.name}
                  </TableCell>
                  <TableCell align="center">
                    {new Date().toISOString()}
                  </TableCell>
                  <TableCell align="center">
                    {JSON.stringify(row.location)}
                  </TableCell>
                  <TableCell align="center">{row.user.contactNumber}</TableCell>
                  <TableCell align="center">{row.user.type}</TableCell>
                  <TableCell align="center">
                    <Button
                      variant="contained"
                      color="primary"
                      disabled={performingAction}
                      onClick={() => this.handleMarkCompleted(row.id)}
                    >
                      Mark Completed
                    </Button>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    );
  }

  componentDidMount() {
    this.fetchAssignedDistress();
  }
}

AssignedTable.propTypes = {
  // Styling
  classes: PropTypes.object.isRequired,

  // Properties
  user: PropTypes.object,
};

export default withRouter(withStyles(styles)(AssignedTable));
