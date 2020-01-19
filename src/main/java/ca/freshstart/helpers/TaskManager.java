package ca.freshstart.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TaskManager implements DisposableBean {

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public void submitTask(TaskFunction task) {

        executor.submit(task::invoke);
    }

    @Override
    public void destroy() throws Exception {
        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    public interface TaskFunction {
        void invoke();
    }
}
