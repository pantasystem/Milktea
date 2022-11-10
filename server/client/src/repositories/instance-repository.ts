import { z } from "zod";
import { tokenRepository } from ".";
import { InstanceSchema } from "../models/instance"
import { InstanceInfoSchema } from "../models/instance-info";

export class InstanceRepository {

    getInstances = async () => {
        const res = await fetch("/api/admin/instances", {
            headers: {
              "Authorization": `Bearer ${tokenRepository.getToken()}`
            }
        })
        const result =  await z.array(InstanceSchema).safeParseAsync(await res.json());
        if (result.success) {
            return result.data
        } else {
            throw result.error
        }
    }
    
    approve = async (instanceId: string) => {
        await fetch(`/api/admin/instances/${instanceId}/approve`, {
            headers: {
              "Authorization": `Bearer ${tokenRepository.getToken()}`
            },
            method: "POST"
        });
    }

    get = async (instanceId: string) => {
        const res = await fetch(`/api/admin/instances/${instanceId}`, {
            headers: {
                "Authorization": `Bearer ${tokenRepository.getToken()}`
            },
            method: "GET"
        });
        const result = await InstanceSchema.safeParseAsync(await res.json());
        if (result.success) {
            return result.data;
        } else {
            throw result.error;
        }
    }

    getInstanceInfo = async (host: string) => {
        const res = await fetch(`/api/admin/instance-with-meta/${host}`, {
            headers: {
                "Authorization": `Bearer ${tokenRepository.getToken()}`
            },
            method: "GET"
        });
        const reuslt = await InstanceInfoSchema.safeParseAsync(await res.json());
        if (reuslt.success) {
            return reuslt.data;
        } else {
            throw reuslt.error;
        }
    }
    
}

const instanceRepository = new InstanceRepository()

export {instanceRepository}