var functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const actionTypeNewLike = "new_like"
const actionTypeNewComment = "new_comment"
const notificationTitle = "Social App"

exports.pushNotificationLikes = functions.database.ref('/post-likes/{postId}/{authorId}/{likeId}').onWrite(event => {

    console.log('New like was added');

    const likeAuthorId = event.params.authorId;
    const postId = event.params.postId;

    // Get liked post.
    const getPostTask = admin.database().ref(`/posts/${postId}`).once('value');

    return getPostTask.then(post => {

        // Get the list of device notification tokens.
        const getDeviceTokensTask = admin.database().ref(`/profiles/${post.val().authorId}/notificationTokens`).once('value');
        console.log('getDeviceTokensTask path: ', `/profiles/${post.val().authorId}/notificationTokens`)

        // Get like author.
        const getLikeAuthorProfileTask = admin.database().ref(`/profiles/${likeAuthorId}`).once('value');

        Promise.all([getDeviceTokensTask, getLikeAuthorProfileTask]).then(results => {
            const tokensSnapshot = results[0];
            const likeAuthorProfile = results[1].val();

            // Check if there are any device tokens.
            if (!tokensSnapshot.hasChildren()) {
                return console.log('There are no notification tokens to send to.');
            }

            console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
            console.log('Fetched like Author profile', likeAuthorProfile);

            // Create a notification
            const payload = {
                notification: {
                    title: notificationTitle,
                    body: `${likeAuthorProfile.username} liked your post`,
                    icon: post.val().imagePath,
                    sound: 'default'
                },
                data : {
                    actionType: actionTypeNewLike,
                    postId: postId,

                },
            };

            // Listing all tokens.
            const tokens = Object.keys(tokensSnapshot.val());
            console.log('tokens:', tokens[0]);

            // Send notifications to all tokens.
            return admin.messaging().sendToDevice(tokens, payload).then(response => {
                        // For each message check if there was an error.
                        const tokensToRemove = [];
                response.results.forEach((result, index) => {
                    const error = result.error;
                    if (error) {
                        console.error('Failure sending notification to', tokens[index], error);
                        // Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                            error.code === 'messaging/registration-token-not-registered') {
                            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                        }
                    }
                });
                return Promise.all(tokensToRemove);
            });
        });
    })
});

exports.pushNotificationComments = functions.database.ref('/post-comments/{postId}/{commentId}').onWrite(event => {

    const commentId = event.params.commentId;
    const postId = event.params.postId;
    const comment = event.data.val();

    console.log('New comment was added, id: ', postId);

    // Get the commented post .
    const getPostTask = admin.database().ref(`/posts/${postId}`).once('value');

    return getPostTask.then(post => {

        // Get the list of device notification tokens.
        const getDeviceTokensTask = admin.database().ref(`/profiles/${post.val().authorId}/notificationTokens`).once('value');
        console.log('getDeviceTokensTask path: ', `/profiles/${post.val().authorId}/notificationTokens`)

        // Get post author.
        const getCommentAuthorProfileTask = admin.database().ref(`/profiles/${comment.authorId}`).once('value');
        console.log('getCommentAuthorProfileTask path: ', `/profiles/${comment.authorId}`)

        Promise.all([getDeviceTokensTask, getCommentAuthorProfileTask]).then(results => {
            const tokensSnapshot = results[0];
            const commentAuthorProfile = results[1].val();

            // Check if there are any device tokens.
            if (!tokensSnapshot.hasChildren()) {
                return console.log('There are no notification tokens to send to.');
            }

            console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');

            // Create a notification
            const payload = {
                notification: {
                    title: notificationTitle,
                    body: `${commentAuthorProfile.username} commented your post`,
                    icon: post.val().imagePath,
                    sound: 'default'
                },
                data : {
                    actionType: actionTypeNewComment,
                    postId: postId,
                },
            };

            // Listing all tokens.
            const tokens = Object.keys(tokensSnapshot.val());
            console.log('tokens:', tokens[0]);

            // Send notifications to all tokens.
            return admin.messaging().sendToDevice(tokens, payload).then(response => {
                        // For each message check if there was an error.
                        const tokensToRemove = [];
                response.results.forEach((result, index) => {
                    const error = result.error;
                    if (error) {
                        console.error('Failure sending notification to', tokens[index], error);
                        // Cleanup the tokens who are not registered anymore.
                        if (error.code === 'messaging/invalid-registration-token' ||
                            error.code === 'messaging/registration-token-not-registered') {
                            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                        }
                    }
                });
                return Promise.all(tokensToRemove);
            });
        });
    })
});

