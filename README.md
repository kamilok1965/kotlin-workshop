coroutines-workshop
=============

Workshop on kotlin coroutines core library

The purpose of this workshop is to try out few basic, but probably most useful mechanisms provided by kotlin coroutines.
We will build distributed system for decrypting hashed passwords. Input data - encrypted text - will be delivered by the external web server. Client library, which we will be implementing during this workshop, will be responsible for fetching encrypted passwords, decrypting them, and sending them back to the central server.


Whole workshop is divided into few self-sufficient sections. We will start with most basic prototype, and will try to gradually make it more efficient and less error prone.

#### 1. Connect and register

At the beginning you need to connect to leaderboard server and send Register message with your name.

You can use Api object that is already created inside main function in file `app.kt`

Once correctly registered your name should appear at our leader board (at [kotlin-coroutines.herokuapp.com](kotlin-coroutines.herokuapp.com))

![](leaderboard.png)

#### 2. Process passwords

Once registered, use acquired token request and check decrypted passwords. **Beware!** Decryper sometimes fails so be sure that you are able to overcome that.

Your client should continuously decrypt passwords. Once you send properly processed password your score on leaderboard should improve.

#### 3. Work parallelization

Now we care more about speed of processing. To measure this we need to use different mode of leader board: [kotlin-coroutines.herokuapp.com](kotlin-coroutines.herokuapp.com)

If we can't speed up decrypting process we can do it parallel. We suggest you to create channels for incoming passwords and finished decryptions. You will also need something that will create worker coroutines. 

Beware: Decrypter has limitation: It can effectively process only 4 computation in one time. Rest will just wait for free slot.

*Tips:* [Channel](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental.channels/-channel.html), [Channel.send](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental.channels/-send-channel/send.html), [Channel.receive](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental.channels/-receive-channel/receive.html), [launch](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/launch.html)

#### 4. Error handling, supervision

This is time to focus also on correctness of your decryption (dealing with Decryptor problems to be more precise).

It is time to finally use full leader board: [kotlin-coroutines.herokuapp.com](kotlin-coroutines.herokuapp.com)

I assume that your are able to recover from exceptions now but did you measure error rates?

Once state in Decryptor gets corrupted all existing instances produce bad results. 
What is even worse all work that was already in progress when state gets corrupted will also yield bad results.
It means that we need to throw away some output from corrupted Decryptor instances. 

Of course we don't want to give up our prallelizaion here.

Consider closing and recreating channel that is used to transmit decryption results to main coroutine. It may be handy to cancel ongoing jobs. Remember that jobs have a hierarchy and scopes.

100% of correctness is realy hard to achieve with parallelization. Everything over 90% is fine :)

*Tips:* [Job](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-job.html), [Job.cancel](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/kotlin.coroutines.experimental.-coroutine-context/cancel.html), [SendChannel.close](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental.channels/-send-channel/close.html), [coroutineScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/coroutine-scope.html), [supervisorScope](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/supervisor-scope.html)

#### 5. Terminating the straglers

To use decryption library we have to do few API calls chained together, i.e. C(B(A(encrypted_message))). Each of those functions is computationally expensive. It could be beneficial to be able to stop the coroutine at any point of computations, because if we already know that the result will be incorrect we have no interest in continuing.  
You can split time consuming computations with yileding to dispatcher or manualy checking if your coroutine is still active. It will be nice to create some little inline function that divides coroutine code into smaller chunks of computation with possibility of cancellation inbetween.

*Tips:* [CoroutineScope.isActive](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/-coroutine-scope/is-active.html), [yield](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental/yield.html)


#### 6. Retrying failed computations
You can see on the leaderboard that lots of passwords that you obtained are never processed, because their computations were cancelled, and passwords themselves were discarded and forgotten. But you can imagine situation where theese passwords are resources that are hard to generate. Then it may be usefull to have a possibility to retry failed computations. You can achieve this by creating channel, to which all failed or cancelled worker coroutines can send their unprocessed passwords. Then you can merge this channel into one with passwords received from server.

*Tips:* [produce](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental.channels/produce.html), [select](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental.selects/select.html), [ReceiveChannel.onReceive](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.experimental.channels/-receive-channel/on-receive.html)
