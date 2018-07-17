const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const actionTypeNewLike = "new_like";
const actionTypeNewComment = "new_comment";
const actionTypeNewPost = "new_post";
const notificationTitle = "Social App";

const followingPosDbValue = "following_post";
const followingPosDbKey = "followingPostsIds";
const followingsDbKey = "followings";
const followersDbKey = "followers";
const followingDbKey = "follow";

const postsTopic = "postsTopic";

const THUMB_MEDIUM_SIZE = 1024; //px
const THUMB_SMALL_SIZE = 100; //px

const THUMB_MEDIUM_DIR = "medium";
const THUMB_SMALL_DIR = "small";

const gcs = require('@google-cloud/storage')();
const path = require('path');
const sharp = require('sharp');

exports.pushNotificationLikes = functions.database.ref('/post-likes/{postId}/{authorId}/{likeId}').onCreate((snap, context)  => {

    console.log('New like was added');

    const likeAuthorId = context.params.authorId;
    const postId = context.params.postId;

    // Get liked post.
    const getPostTask = admin.database().ref(`/posts/${postId}`).once('value');

    return getPostTask.then(post => {

        if (likeAuthorId === post.val().authorId) {
            console.log('User liked own post');
            return 'User liked own post';
        }

        // Get the list of device notification tokens.
        const getDeviceTokensTask = admin.database().ref(`/profiles/${post.val().authorId}/notificationTokens`).once('value');
        console.log('getDeviceTokensTask path: ', `/profiles/${post.val().authorId}/notificationTokens`);

        // Get like author.
        const getLikeAuthorProfileTask = admin.database().ref(`/profiles/${likeAuthorId}`).once('value');

        return Promise.all([getDeviceTokensTask, getLikeAuthorProfileTask]).then(results => {
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
                data: {
                    actionType: actionTypeNewLike,
                    title: notificationTitle,
                    body: `${likeAuthorProfile.username} liked your post`,
                    icon: post.val().imagePath,
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
        }).catch((fallback) => {
            console.error('Failure getPostTask', fallback);
        });
    }).catch((fallback) => {
        console.error('Failure getPostTask', fallback);
    })
});

exports.pushNotificationComments = functions.database.ref('/post-comments/{postId}/{commentId}').onCreate((snap, context) => {

    const commentId = context.params.commentId;
    const postId = context.params.postId;
    const comment = snap.val();

    console.log('New comment was added, id: ', postId);

    // Get the commented post .
    const getPostTask = admin.database().ref(`/posts/${postId}`).once('value');

    return getPostTask.then(post => {

        // Get the list of device notification tokens.
        const getDeviceTokensTask = admin.database().ref(`/profiles/${post.val().authorId}/notificationTokens`).once('value');
        console.log('getDeviceTokensTask path: ', `/profiles/${post.val().authorId}/notificationTokens`);

        // Get post author.
        const getCommentAuthorProfileTask = admin.database().ref(`/profiles/${comment.authorId}`).once('value');
        console.log('getCommentAuthorProfileTask path: ', `/profiles/${comment.authorId}`);

        return Promise.all([getDeviceTokensTask, getCommentAuthorProfileTask]).then(results => {
            const tokensSnapshot = results[0];
            const commentAuthorProfile = results[1].val();

            if (commentAuthorProfile.id === post.val().authorId) {
                console.log('User commented own post');
                return 'User commented own post';
            }

            // Check if there are any device tokens.
            if (!tokensSnapshot.hasChildren()) {
                console.log('There are no notification tokens to send to.');
                return 'There are no notification tokens to send to.';
            }

            console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');

            // Create a notification
            const payload = {
                data: {
                    actionType: actionTypeNewComment,
                    title: notificationTitle,
                    body: `${commentAuthorProfile.username} commented your post`,
                    icon: post.val().imagePath,
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
        }).catch((fallback) => {
            console.error('Failure getPostTask', fallback);
        });
    }).catch((fallback) => {
        console.error('Failure getPostTask', fallback);
    })
});

exports.pushNotificationNewPost = functions.database.ref('/posts/{postId}').onCreate((snap, context) => {
    const postId = context.params.postId;

    console.log('New post was created');

    // Get post authorID.
    const getAuthorIdTask = admin.database().ref(`/posts/${postId}/authorId`).once('value');

    return getAuthorIdTask.then(authorId => {

        console.log('post author id', authorId.val());

        // Create a notification
        const payload = {
            data: {
                actionType: actionTypeNewPost,
                postId: postId,
                authorId: authorId.val(),
            },
        };

        // Send a message to devices subscribed to the provided topic.
        return admin.messaging().sendToTopic(postsTopic, payload).then(response => {
                // See the MessagingTopicResponse reference documentation for the
                // contents of response.
                console.log("Successfully sent info about new post :", response);
                return response;
            })
            .catch(error => {
                console.log("Error sending info about new post:", error);
            });
    }).catch(fallback => {
        console.error('Failure getPostTask', fallback);
    });

});


exports.addNewPostToFollowers = functions.database.ref('/posts/{postId}').onCreate((snap, context) => {
    const postId = context.params.postId;

    console.log('New post was created');

    // Get post authorID.
    const getAuthorIdTask = admin.database().ref(`/posts/${postId}/authorId`).once('value');

    return getAuthorIdTask.then(authorId => {

        console.log('post author id', authorId.val());

        // Get followers ids.
        return admin.database().ref().child(followingDbKey).child(authorId.val()).child(followersDbKey).once('value', function(snapshot) {
            snapshot.forEach(function (childSnapshot) {
                let followerId = childSnapshot.val().profileId;
                console.log('setNewPostValuesToFollower', "followerId:", followerId, "postId:", postId);

                admin.database().ref().child(followingPosDbKey).child(followerId).child(postId).set({
                    postId:postId
                });
            });
        }).catch(fallback => {
            console.error('Failure get followers ids', fallback);
        });

    }).catch(fallback => {
        console.error('Failure getPostTask', fallback);
    });

});


exports.removePostFromFollowingList = functions.database.ref('/posts/{postId}').onDelete((snap, context) => {
    const postId = context.params.postId;
    const authorId = snap.val().authorId;

    // Get followers ids.
    return admin.database().ref().child(followingDbKey).child(authorId).child(followersDbKey).once('value', function(snapshot) {
        snapshot.forEach(function (childSnapshot) {
            let followerId = childSnapshot.val().profileId;
            admin.database().ref().child(followingPosDbKey).child(followerId).child(postId).remove();
            console.log('remove post if from following list for followerId:', followerId, "postId:", postId);
        });
    }).catch(fallback => {
        console.error('Failure get followers ids', fallback);
    });
});

/**
 * When an image is uploaded in the Storage bucket We generate a thumbnail automatically using
 * Sharp.
 */
exports.generateThumbnail = functions.storage.object().onFinalize((object) => {
    const fileBucket = object.bucket; // The Storage bucket that contains the origin file.
    const filePath = object.name; // File path in the bucket.
    const contentType = object.contentType; // File content type.

    // Exit if this is triggered on a file that is not an image.
    if (!contentType.startsWith('image/')) {
        console.log('This is not an image.');
        return null;
    }

    // Get the dir name.
    const dirname = path.dirname(filePath);
    if (dirname.includes(THUMB_MEDIUM_DIR) || dirname.includes(THUMB_SMALL_DIR)) {
        console.log('Already scaled.');
        return null;
    }

    // Download file from bucket.
    const bucket = gcs.bucket(fileBucket);

    const mediumThumbPromise = createThumb(filePath, THUMB_MEDIUM_DIR, THUMB_MEDIUM_SIZE, contentType, bucket);
    const smallThumbPromise = createThumb(filePath, THUMB_SMALL_DIR, THUMB_SMALL_SIZE, contentType, bucket);

    return Promise.all([mediumThumbPromise, smallThumbPromise]).then(() => {
        console.log('Thumbnail created successfully');
        return null;
    });
});

function createThumb(originFilePath, thumbDir, size, contentType, bucket) {
    const metadata = {
        contentType: contentType,
    };

    // Get the file name.
    const fileName = path.basename(originFilePath);
    const thumbFilePath = path.join(path.dirname(originFilePath), thumbDir, fileName);
    console.log("thumb file path", thumbFilePath);

    const thumbnailUploadStream = bucket.file(thumbFilePath).createWriteStream({metadata});

    // Create Sharp pipeline for resizing the image and use pipe to read from bucket read stream
    const pipeline = sharp();
    pipeline
        .resize(size, size)
        .max()
        .pipe(thumbnailUploadStream);

    bucket.file(originFilePath).createReadStream().pipe(pipeline);

    return new Promise((resolve, reject) =>
        thumbnailUploadStream.on('finish', resolve).on('error', reject));
}
