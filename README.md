# Software Engineer Test Task Submission

Adding the original task descriptions here with some words about the solution and some questions I would ask if this was a part of the development. 
Those questions should be aligned with the person responsible for the product. 
Putting in braces what the current solution is and if there would be any ideas for improving it.

## Task 1
> Come up with ticket score algorithm that accounts for rating category weights (available in `rating_categories` table). Ratings are given in a scale of 0 to 5. Score should be representable in percentages from 0 to 100.

#### Solution
Implemented a ticket score algorithm which uses the category weight when calculating the score. So if the weight was over 1, then there is a bigger chance of the score being higher with a lower rating.

#### Questions
1. When calculating the score percentage, should the percentage be rounded up or down? (Implemented it in a way that it would be floored. So if we had 32.6% for example, then it would be 32%)
2. Are decimal points important? For example, do we care if we represent the value as 32.6 or should it remain as an integer like 32. (Current solution returns integer)
----
## Task 2
> Build a service that can be queried using gRPC calls and can answer following questions
----
### Task 2.1
> Aggregated category scores over a period of time
#### Solution
Added ScoreResource.CategoryScoresOverPeriod for fetching aggregated category scores over a period of time. If period is longer than a month, it will aggregate the values by weeks. Returns the dates as a range, so if we aggregate daily then one day is defined as day1-day2 (for day1 values). If weekly then it is defined as day1-day7.
#### Example usage
```bash
grpcurl --plaintext -d '{"periodStart": "2019-03-01T00:00:00Z", "periodEnd": "2019-03-30T20:59:59Z"}' localhost:9090 ScoreResource.CategoryScoresOverPeriod
```
#### Questions regarding implementation
1. Do all of the categories have to be present in the answer?
2. What is the hard limit that would be considered `longer than one month` this would be good to know regarding implementation. (Currently using Javas implementation to calculate the date differences and seeing if the differnce between the dates are over a month.)
3. (More of a specifying question) If we aggregate daily, do we want the scores to only contain the scores for the given day. ()
4. (More of a specifying question) If we aggregate weekly, do we want the scores to only contain the scores for the given weekdays.
----
### Task 2.2
> Scores by ticket
#### Solution
Added ScoreResource.ScoresByTicket for fetching scores by tickets. It fetches all the tickets for the period and then fetches all the ratings for the tickets. Afterward matches the ratings by tickets and does the matching of the categories.
#### Example usage
```bash
grpcurl --plaintext -d '{"periodStart": "2019-01-01T00:00:00Z", "periodEnd": "2020-12-31T23:59:59Z"}' localhost:9090 ScoreResource.ScoresByTicket
```
#### Questions regarding implementation
1. Are the fetched ratings somehow affected by the given request body? (Currently fetching all the ratings for the movie without checking the creation timestamp)
2. Is it fine if the service returns only the category values that were found? If there are some missing categories, do they need to be present?
3. Seeing as this request response can go really large, would it be reasonable to add pagination to the service?
---
### Task 2.3
> Overall quality score
#### Solution
Added ScoreResource.OverallQualityScoreRequest for fetching the overall quality score. Fetches all the ratings for the period. Calculates the score for each of the rating based on the Task 1 algorithm. Averages all the scores using simple average.
#### Example usage
```bash
grpcurl --plaintext -d '{"periodStart": "2019-01-01T00:00:00Z", "periodEnd": "2020-12-31T23:59:59Z"}' localhost:9090 ScoreResource.OverallQualityScore
```
#### Questions regarding implementation
1. Question regarding the simple average, should the resulting average be rounded up or down? (Current implementation is flooring it, so 32.6% would be 32%)
---
### Task 2.4
> Period over Period score change
#### Solution
Added ScoreResource.PeriodOverPeriodScoreChange for fetching period over period score. Input is a selected period. The service will calculate a previous period based on the selected period range. Using both the ranges, two separate percentages are returned to show the differnce.
#### Example usage
```bash
grpcurl --plaintext -d '{"selectedPeriodStart": "2020-01-01T00:00:00Z", "selectedPeriodEnd": "2020-12-31T00:00:00Z"}' localhost:9090 ScoreResource.PeriodOverPeriodScoreChange
```
#### Questions regarding implementation
1. Is there a need to show the difference as a number or is returning the percentages as numbers good enough?
2. Would it be reasonable to provide the previous period as an input as well because it might become difficult for the server to do some previous period findings. It could also provide beneficial to compare different periods to each other (like weeks to months).

### Bonus question
> How would you build and deploy the solution?
#### Solution
I would dockerize the application by creating a Dockerfile. The Dockerfile would allow me to create a docker image that could be then used for deploying.
As Klaus is using Kubernetes then the image should be uploaded to the cloud providers docker image repository (like AWS has Elastic Container Registry).
Assuming that the Kubernetes service has access to the registry, there should be some configuration file done for deploying the docker image.
If the configuration file is present, Kubernetes can deploy the solution as defined by the configuration file.
(Hopefully there is already a CI/CD pipeline in Klaus that handles this)

----
# Improvements
As the solution is not a final solution code-wise, I am marking here some improvements that could be made
1. If there would be a RDBMS present, I would consider adding indexes to some of the tables that are using datetime to filter the results.
2. (Related to previous) Would consider using a cache for entities that are being fetched eagerly
3. Observability wise tracing would be something nice to have, so could see what methods are taking longer than usual
4. Logging and exception handling could be improved to provide more insight into the service internals
5. More test cases so apparent edge cases could also be covered
6. There seems to be some issue when sending google.protobuf.Timestamp because converting it to Java LocalDateTime will add a few hours (possibly from the time zone). That could be improved
7. Documentation of the gRPC endpoints with examples
----
(Keeping the initial READ.ME here as well for reference)
# Software Engineer Test Task

As a test task for [Klaus](https://www.klausapp.com) software engineering position we ask our candidates to build a small [gRPC](https://grpc.io) service using language of their choice. Preferred language for new services in Klaus is [Go](https://golang.org).

The service should be using provided sample data from SQLite database (`database.db`).

Please fork this repository and share the link to your solution with us.

### Tasks

1. Come up with ticket score algorithm that accounts for rating category weights (available in `rating_categories` table). Ratings are given in a scale of 0 to 5. Score should be representable in percentages from 0 to 100. 

2. Build a service that can be queried using [gRPC](https://grpc.io/docs/tutorials/basic/go/) calls and can answer following questions:

    * **Aggregated category scores over a period of time**
    
        E.g. what have the daily ticket scores been for a past week or what were the scores between 1st and 31st of January.

        For periods longer than one month weekly aggregates should be returned instead of daily values.

        From the response the following UI representation should be possible:

        | Category | Ratings | Date 1 | Date 2 | ... | Score |
        |----|----|----|----|----|----|
        | Tone | 1 | 30% | N/A | N/A | X% |
        | Grammar | 2 | N/A | 90% | 100% | X% |
        | Random | 6 | 12% | 10% | 10% | X% |

    * **Scores by ticket**

        Aggregate scores for categories within defined period by ticket.

        E.g. what aggregate category scores tickets have within defined rating time range have.

        | Ticket ID | Category 1 | Category 2 |
        |----|----|----|
        | 1   |  100%  |  30%  |
        | 2   |  30%  |  80%  |
      * Improvement idea: Should consider pagination and what order the data should be returned in

    * **Overall quality score**

        What is the overall aggregate score for a period.

        E.g. the overall score over past week has been 96%.

    * **Period over Period score change**

        What has been the change from selected period over previous period.

        E.g. current week vs. previous week or December vs. January change in percentages.


### Bonus

* How would you build and deploy the solution?

    At Klaus we make heavy use of containers and [Kubernetes](https://kubernetes.io).
